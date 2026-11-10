#!/usr/bin/env python3
"""Run only against a synthetic demo database. Uses Python standard library."""
import json, os, urllib.request, urllib.error, uuid, concurrent.futures, datetime
BASE=os.environ.get('HMS_TEST_URL','http://localhost:8080/api')
ADMIN=os.environ['HMS_ADMIN_PASSWORD']; DEMO=os.environ['HMS_DEMO_PASSWORD']
checks=[]
def request(method,path,data=None,token=None,expected=200):
    headers={'Content-Type':'application/json'}
    if token:headers['Authorization']='Bearer '+token
    req=urllib.request.Request(BASE+path,data=None if data is None else json.dumps(data).encode(),headers=headers,method=method)
    try:
        with urllib.request.urlopen(req,timeout=15) as response:code=response.status;body=response.read()
    except urllib.error.HTTPError as e:code=e.code;body=e.read()
    assert code==expected, f'{method} {path}: expected {expected}, got {code}: {body[:500]!r}'
    checks.append(f'{method} {path}: {code}')
    return json.loads(body) if body and body[:1] in (b'{',b'[') else None

def login(username,password):return request('POST','/auth/login',{'username':username,'password':password})['token']
a=login('admin',ADMIN);doctor=login('demo.doctor',DEMO);nurse=login('demo.nurse',DEMO)
request('GET','/patients',expected=401)
request('GET','/patients',token='invalid',expected=401)
p=request('POST','/patients',{'firstName':'Synthetic Smoke','mobile':'0000000000'},a)
assert p['uhid'].startswith('MRN-')
request('POST','/patients',{'firstName':'Unauthorized'},doctor,403)
request('GET','/radiology',token=doctor)
request('GET','/admissions',token=doctor)
request('GET','/hr/me/salary',token=doctor)
request('GET','/hr/me/salary',token=a,expected=403) # bootstrap admin intentionally has no employee
request('DELETE','/employees/1',token=a,expected=409)
u='smoke.'+uuid.uuid4().hex[:10]
request('POST','/public/register',{'username':u,'password':DEMO,'firstName':'Synthetic Self Service','mobile':'0000000001'})
t=login(u,DEMO)
slots=request('GET','/public/slots')
assert len(slots)>2
request('POST','/patient/book',{'slotId':slots[0]['id']},t,400)
request('POST','/patient/registration-fee-demo',{},t)
appointment=request('POST','/patient/book',{'slotId':slots[0]['id']},t)
request('POST','/scheduling/book',{'patientId':p['id'],'slotId':slots[0]['id']},a,409)
request('POST',f"/patient/appointments/{appointment['id']}/reschedule",{'slotId':slots[1]['id']},t)
assert any(x['id']==slots[0]['id'] for x in request('GET','/public/slots'))
request('POST',f"/patient/appointments/{appointment['id']}/reschedule-request",{'reason':'Synthetic reception request'},t)
request('POST',f"/patient/appointments/{appointment['id']+9999}/reschedule",{'slotId':slots[2]['id']},t,404)
visit=request('POST',f"/workflow/appointments/{appointment['id']}/check-in",{},a)
request('POST','/care/teams',{'patientId':visit['patientId'],'username':'demo.nurse'},a)
request('POST',f"/workflow/encounters/{visit['id']}/vitals",{'temperature':37,'pulse':72,'spo2':99},nurse)
request('POST',f"/workflow/encounters/{visit['id']}/start-consultation",{},nurse,403)
request('POST',f"/workflow/encounters/{visit['id']}/start-consultation",{},doctor)
request('POST',f"/workflow/encounters/{visit['id']}/consultation",{'diagnosis':'Synthetic QA only'},doctor)
request('POST','/operations/lab/orders',{'encounterId':visit['id'],'tests':[{'testCode':'CBC','processingType':'IN_HOUSE'}]},doctor)
labtech=login('demo.lab_technician',DEMO);pathologist=login('demo.pathologist',DEMO);supervisor=login('demo.lab_supervisor',DEMO)
item=request('GET','/operations/lab/items',token=labtech)[0]
lab_invoice=request('GET','/finance/invoices/'+str(item['invoice_id']),token=a)
request('POST','/finance/invoices/'+str(item['invoice_id'])+'/payments',{'parts':[{'method':'CASH','amount':lab_invoice['total']-lab_invoice['paid']}]},a)
for status in ['BILLED','SAMPLE_COLLECTED','IN_PROCESS','RESULT_ENTERED']:
    request('POST',f"/operations/lab/items/{item['id']}/transition",{'status':status,'resultValue':'Synthetic result'},labtech)
request('POST',f"/operations/lab/items/{item['id']}/transition",{'status':'VERIFIED','verifiedBy':'Synthetic'},labtech,403)
request('POST',f"/operations/lab/items/{item['id']}/transition",{'status':'VERIFIED','verifiedBy':'Synthetic'},pathologist)
request('POST',f"/operations/lab/items/{item['id']}/transition",{'status':'COMPLETED'},supervisor)
assert request('GET','/patient/reports',token=t)
request('POST','/orders/radiology',{'encounterId':visit['id'],'studyName':'Synthetic QA study'},doctor)
rx=request('POST','/orders/prescriptions',{'encounterId':visit['id'],'items':[{'medicineName':'Synthetic QA medicine','dose':'Demo','frequency':'Demo','duration':'Demo'}]},doctor)
assert request('GET','/patient/prescriptions',token=t)==[]
request('POST',f"/orders/prescriptions/{rx['id']}/finalize",{},doctor)
assert request('GET','/patient/prescriptions',token=t)
invoice=request('POST','/operations/billing/invoices',{'patientId':p['id'],'items':[{'description':'Synthetic fee','qty':1,'unitPrice':100}]},a)
request('POST',f"/operations/billing/invoices/{invoice['id']}/payments",{'amount':40,'method':'UPI','referenceNo':'demo'},a,400)
partial=request('POST',f"/operations/billing/invoices/{invoice['id']}/payments",{'amount':40,'method':'CASH'},a)
assert partial['status']=='PARTIALLY_PAID'
request('POST',f"/operations/billing/invoices/{invoice['id']}/payments",{'amount':61,'method':'CASH'},a,409)
full=request('POST',f"/operations/billing/invoices/{invoice['id']}/payments",{'amount':60,'method':'UPI','referenceNo':'SYNTHETIC-VERIFIED','verified':True},a)
assert full['status']=='PAID'
pharmacist=login('demo.pharmacist',DEMO)
stock=request('GET','/inventory',token=pharmacist)[0]
request('POST',f"/operations/pharmacy/inventory/{stock['id']}/receive",{'qty':10},pharmacist,403)
radiologist=login('demo.radiologist',DEMO)
study=request('GET','/radiology',token=radiologist)[0]
for status in ['SCHEDULED','IN_PROCESS','REPORTED','VERIFIED']:
    request('POST',f"/enterprise/radiology/{study['id']}/transition",{'status':status,'report':'Synthetic report only'},radiologist)
hr=login('demo.hr',DEMO)
request('POST','/hr/admin/salary',{'employeeId':1,'salaryMonth':'2026-10','basic':100,'allowances':10,'deductions':5},hr)
assert request('GET','/hr/me/salary',token=doctor)[0]['net_pay']==105
future=datetime.date.today()+datetime.timedelta(days=2)
request('POST','/hr/me/leaves',{'leaveType':'CASUAL','startDate':str(future),'endDate':str(future),'reason':'Synthetic leave'},doctor)
leave=request('GET','/hr/admin/leaves',token=hr)[0]
request('POST',f"/hr/admin/leaves/{leave['id']}/review",{'status':'APPROVED'},hr)
request('POST','/enterprise/roster',{'employeeId':1,'shiftDate':str(future),'startsAt':'09:00','endsAt':'12:00'},hr,409)
request('POST','/enterprise/roster',{'employeeId':1,'shiftDate':str(future+datetime.timedelta(days=15)),'startsAt':'09:00','endsAt':'12:00'},hr)
shift=request('GET','/enterprise/roster',token=hr)[0]
request('POST',f"/enterprise/roster/{shift['id']}/absent",{},hr)
beds=request('GET','/operations/ipd/beds',token=a)
b=beds[0];admission=request('POST','/operations/ipd/admit',{'patientId':p['id'],'ward':b['ward'],'bedNo':b['bed_no']},a)
request('POST','/operations/ipd/admit',{'patientId':p['id'],'ward':b['ward'],'bedNo':b['bed_no']},a,409)
request('POST','/care/teams',{'patientId':p['id'],'username':'demo.doctor'},a)
request('POST','/care/teams',{'patientId':p['id'],'username':'demo.nurse'},a)
med=request('POST','/inpatient/medication-orders',{'admissionId':admission['id'],'inventoryItemId':stock['id'],'dose':'Synthetic','route':'ORAL','startsAt':(datetime.datetime.now()-datetime.timedelta(hours=1)).isoformat(timespec='seconds'),'intervalHours':8,'doseCount':2,'quantityPerDose':1},doctor)
request('POST',f"/inpatient/medication-orders/{med['id']}/verify",{},pharmacist)
request('POST','/pharmacy/dispenses',{'patientId':p['id'],'medicationOrderId':med['id'],'inventoryItemId':stock['id'],'quantity':2},pharmacist)
m=next(x for x in request('GET','/ward/medications',token=nurse) if x['medication_order_id']==med['id'])
request('POST',f"/ward/medications/{m['id']}/administer",{'status':'GIVEN'},nurse)
request('POST',f"/ward/medications/{m['id']}/administer",{'status':'GIVEN'},nurse,409)
insurer=login('demo.insurance',DEMO)
claim=request('POST','/enterprise/claims',{'admissionId':admission['id'],'provider':'Synthetic Insurer','policyNo':'DEMO','claimType':'CASHLESS','claimAmount':1000,'nonPayable':100},insurer)
for status in ['PREAUTH_SUBMITTED','APPROVED','FINAL_SUBMITTED']:
    request('POST',f"/enterprise/insurance/{claim['id']}/transition",{'status':status,'amount':900,'notes':'Synthetic test'},insurer)
claim_bill=request('POST','/operations/billing/invoices',{'patientId':p['id'],'items':[{'description':'Synthetic cashless invoice','qty':1,'unitPrice':1000}]},a)
request('POST',f"/coverage/claims/{claim['id']}/allocations",{'invoiceId':claim_bill['id'],'amount':900},insurer)
request('POST',f"/coverage/claims/{claim['id']}/settlements",{'amount':900,'referenceNo':'SYNTHETIC-SETTLEMENT','receivedOn':str(datetime.date.today())},insurer)
request('POST',f"/operations/ipd/admissions/{admission['id']}/discharge",{},a)
request('POST',f"/ward/beds/{b['id']}/ready",{},nurse)
request('POST','/helpdesk/tickets',{'subject':'Synthetic QA','description':'Synthetic test ticket','priority':'NORMAL'},t)
assert all(x['created_by']==u for x in request('GET','/helpdesk/tickets',token=t))
request('GET','/analytics/monthly?month=2026-10',token=a)
request('GET','/analytics/monthly?month=invalid',token=a,expected=400)
# Two concurrent attempts for the same slot: precisely one must succeed.
race_slot=request('GET','/public/slots')[0]['id']
def compete(_):
    req=urllib.request.Request(BASE+'/scheduling/book',data=json.dumps({'patientId':p['id'],'slotId':race_slot}).encode(),headers={'Content-Type':'application/json','Authorization':'Bearer '+a},method='POST')
    try:
        with urllib.request.urlopen(req,timeout=15) as r:return r.status
    except urllib.error.HTTPError as e:return e.code
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
    statuses=sorted(pool.map(compete,range(2)))
assert statuses==[200,409],statuses
checks.append('Concurrent slot reservation: one 200, one 409')
print(json.dumps({'passed':len(checks),'checks':checks},indent=2))
