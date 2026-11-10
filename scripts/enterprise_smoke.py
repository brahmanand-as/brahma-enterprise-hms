#!/usr/bin/env python3
"""Extended acceptance scenarios. Requires a fresh synthetic demo database."""
from pathlib import Path
Path('tools').mkdir(exist_ok=True)
exec(compile((Path(__file__).parent/'api_smoke.py').read_text(),'api_smoke.py','exec'),globals())
from datetime import date,timedelta
# Custom roles are exact permission sets and are refreshed on every JWT request.
request('POST','/security/roles',{'name':'SYNTHETIC_VIEWER','label':'Synthetic records reviewer','permissions':['patients.read']},a)
request('POST','/staff',{'username':'demo.viewer','password':DEMO,'role':'SYNTHETIC_VIEWER'},a)
viewer=login('demo.viewer',DEMO)
request('GET','/patients',token=viewer)
request('POST','/patients',{'firstName':'Forbidden'},viewer,403)
request('GET','/finance/invoices',token=viewer,expected=403)
request('GET','/security/roles',token=viewer,expected=403)
request('PUT','/security/roles/SYNTHETIC_VIEWER',{'permissions':['billing.read']},a)
request('GET','/patients',token=viewer,expected=403)
request('GET','/finance/invoices',token=viewer)
accounts=request('GET','/security/accounts',token=a)
viewer_id=next(x['id'] for x in accounts if x['username']=='demo.viewer')
admin_id=next(x['id'] for x in accounts if x['username']=='admin')
request('PATCH','/security/accounts/'+str(viewer_id),{'enabled':False},a)
request('GET','/finance/invoices',token=viewer,expected=401)
request('PATCH','/security/accounts/'+str(viewer_id),{'enabled':True},a)
request('GET','/finance/invoices',token=viewer,expected=401)
viewer=login('demo.viewer',DEMO)
request('PATCH','/security/accounts/'+str(admin_id),{'enabled':False},a,409)
request('POST','/security/roles',{'name':'BAD_SECURITY','label':'Invalid escalation','permissions':['security.manage']},a,400)
# Duplicate self-service identity cannot create a second MRN/registration fee.
request('POST','/public/register',{'username':'duplicate.patient','password':DEMO,'firstName':'Synthetic Self Service','mobile':'0000000001'},expected=409)
# Clinicians cannot read/write an unassigned patient's record.
unassigned=request('POST','/patients',{'firstName':'Unassigned synthetic'},a)
request('GET','/patients/'+str(unassigned['id']),token=doctor,expected=404)
assert all(x['id']!=unassigned['id'] for x in request('GET','/patients',token=doctor))
# Auto-issued final documents are immutable, patient-scoped and valid PDF files.
issued=request('GET','/patient/documents',token=t)
assert len(issued)>=2
for d in issued:
    req=urllib.request.Request(BASE+f"/patient/documents/{d['id']}/pdf",headers={'Authorization':'Bearer '+t})
    with urllib.request.urlopen(req) as response:pdf=response.read()
    assert pdf.startswith(b'%PDF-') and len(pdf)>1000
    if d['document_type']=='PRESCRIPTION':Path('tools/qa-prescription.pdf').write_bytes(pdf)
    checks.append('Issued '+d['document_type']+' PDF: valid header and nonempty content')
request('POST','/documents/issue',{'type':'PRESCRIPTION','referenceId':99999},a,404)
# Split payments validate every part before recording any transaction.
fin=request('POST','/operations/billing/invoices',{'patientId':p['id'],'items':[{'description':'Synthetic combined fee','qty':1,'unitPrice':100}]},a)
request('POST',f"/finance/invoices/{fin['id']}/payments",{'parts':[{'method':'CASH','amount':40},{'method':'UPI','amount':60,'referenceNo':'UNVERIFIED'}]},a,400)
assert request('GET',f"/finance/invoices/{fin['id']}",token=a)['paid']==0
request('POST',f"/finance/invoices/{fin['id']}/payments",{'parts':[{'method':'CASH','amount':40},{'method':'UPI','amount':60,'referenceNo':'SYNTHETIC-OK','verified':True}]},a)
request('POST',f"/finance/invoices/{fin['id']}/refunds",{'amount':20,'method':'CASH','referenceNo':'SYNTHETIC-REFUND','reason':'Synthetic adjustment'},a)
request('POST',f"/finance/invoices/{fin['id']}/adjustments",{'type':'CREDIT','amount':20,'reason':'Synthetic fee correction'},a)
assert request('GET',f"/finance/invoices/{fin['id']}",token=a)['status']=='PAID'
# Procurement, batch receipt, FEFO, verified medication schedule and linked credits.
item_new=request('POST','/inventory',{'sku':'QA-'+uuid.uuid4().hex[:8],'name':'Synthetic acceptance medicine','category':'DEMO','unit':'TABLET','stockQty':999,'reorderLevel':2},pharmacist)
assert item_new['stockQty']==0
request('POST','/pharmacy/suppliers',{'name':'Synthetic QA supplier','contact':'DEMO'},pharmacist)
supplier=next(x for x in request('GET','/pharmacy/suppliers',token=pharmacist) if x['name']=='Synthetic QA supplier')
po=request('POST','/pharmacy/purchases',{'supplierId':supplier['id'],'lines':[{'inventoryItemId':item_new['id'],'quantity':10,'unitCost':10}]},pharmacist)
purchase=next(x for x in request('GET','/pharmacy/purchases',token=pharmacist) if x['id']==po['id'])
receipt=request('POST','/pharmacy/receipts',{'purchaseLineId':purchase['lines'][0]['id'],'batchNo':'QA-BATCH-EARLY','expiresOn':str(date.today()+timedelta(days=10)),'quantity':6,'salePrice':20},pharmacist)
request('POST','/pharmacy/receipts',{'purchaseLineId':purchase['lines'][0]['id'],'batchNo':'QA-BATCH-LATER','expiresOn':str(date.today()+timedelta(days=30)),'quantity':4,'salePrice':20},pharmacist)
request('POST','/pharmacy/receipts',{'purchaseLineId':purchase['lines'][0]['id'],'batchNo':'QA-EXCESS','expiresOn':str(date.today()+timedelta(days=40)),'quantity':1,'salePrice':20},pharmacist,409)
bed_available=next(x for x in request('GET','/inpatient/beds',token=a) if x['status']=='AVAILABLE')
request('POST',f"/inpatient/beds/{bed_available['id']}/status",{'status':'RESERVED','patientId':p['id'],'reason':'Synthetic admission reservation'},a)
ad=request('POST','/operations/ipd/admit',{'patientId':p['id'],'ward':bed_available['ward'],'bedNo':bed_available['bed_no']},a)
mo=request('POST','/inpatient/medication-orders',{'admissionId':ad['id'],'inventoryItemId':item_new['id'],'dose':'1 synthetic unit','route':'ORAL','startsAt':(datetime.datetime.now()-datetime.timedelta(hours=1)).isoformat(timespec='seconds'),'intervalHours':8,'doseCount':3,'quantityPerDose':1},doctor)
dose=next(x for x in request('GET','/ward/medications',token=nurse) if x['medication_order_id']==mo['id'])
request('POST',f"/ward/medications/{dose['id']}/administer",{'status':'GIVEN'},nurse,409)
request('POST',f"/inpatient/medication-orders/{mo['id']}/verify",{},pharmacist)
request('POST',f"/ward/medications/{dose['id']}/administer",{'status':'GIVEN'},nurse,409)
disp=request('POST','/pharmacy/dispenses',{'patientId':p['id'],'medicationOrderId':mo['id'],'inventoryItemId':item_new['id'],'quantity':3},pharmacist)
early=next(x for x in request('GET','/pharmacy/batches',token=pharmacist) if x['batch_no']=='QA-BATCH-EARLY')
assert early['quantity']==3
request('POST',f"/ward/medications/{dose['id']}/administer",{'status':'GIVEN','notes':'Synthetic dose administered'},nurse)
request('POST','/inpatient/nursing-notes',{'admissionId':ad['id'],'noteType':'HANDOVER','assessment':'Synthetic patient stable','plan':'Synthetic handover'},nurse)
request('POST',f"/pharmacy/dispenses/{disp['id']}/return",{'quantity':1,'reason':'Synthetic unused unit'},pharmacist)
credit=next(x for x in request('GET','/finance/return-credits',token=a) if x['dispense_id']==disp['id'])
request('POST',f"/finance/return-credits/{credit['id']}/reconcile",{},a)
request('POST',f"/pharmacy/dispenses/{disp['id']}/return",{'quantity':3,'reason':'Invalid return'},pharmacist,409)
request('POST',f"/pharmacy/batches/{early['id']}/action?action=WARD_RECEIPT",{'quantity':1,'reason':'Ward requisition','ward':ad['ward']},pharmacist)
request('POST',f"/pharmacy/ward-stock/{early['id']}/consume",{'quantity':1,'reason':'Synthetic consumable','ward':ad['ward'],'patientId':p['id'],'admissionId':ad['id']},nurse)
# Enhancement rejection retains prior approval; settlement is reconciled in parts.
ci=request('POST','/enterprise/claims',{'admissionId':ad['id'],'provider':'Synthetic insurance QA','policyNo':'QA','claimType':'CASHLESS','claimAmount':1000,'nonPayable':100},insurer)
request('PUT',f"/coverage/claims/{ci['id']}/lines",{'lines':[{'description':'Eligible care','amount':1000,'eligibleAmount':900}]},insurer)
for state,amount in [('PREAUTH_SUBMITTED',700),('APPROVED',700)]:request('POST',f"/enterprise/insurance/{ci['id']}/transition",{'status':state,'amount':amount,'notes':'Synthetic decision'},insurer)
request('POST',f"/coverage/claims/{ci['id']}/enhancements",{'amount':200,'notes':'Synthetic enhancement'},insurer)
eh=next(x for x in request('GET','/coverage/enhancements',token=insurer) if x['claim_id']==ci['id'])
request('POST',f"/coverage/enhancements/{eh['id']}/review",{'status':'REJECTED','amount':0,'notes':'Synthetic rejected enhancement'},insurer)
assert request('GET',f"/coverage/claims/{ci['id']}",token=insurer)['approved_amount']==700
request('POST',f"/enterprise/insurance/{ci['id']}/transition",{'status':'FINAL_SUBMITTED','amount':700,'notes':'Synthetic final submission'},insurer)
case_bill=request('POST','/operations/billing/invoices',{'patientId':p['id'],'items':[{'description':'Synthetic final cashless invoice','qty':1,'unitPrice':1000}]},a)
request('POST',f"/coverage/claims/{ci['id']}/allocations",{'invoiceId':case_bill['id'],'amount':700},insurer)
request('POST',f"/finance/invoices/{case_bill['id']}/payments",{'parts':[{'method':'CASH','amount':301}]},a,409)
part=request('POST',f"/coverage/claims/{ci['id']}/settlements",{'amount':400,'referenceNo':'QA-INSURANCE-1','receivedOn':str(date.today())},insurer)
assert part['status']=='PARTIALLY_SETTLED' and part['insurerOutstanding']==300
settled=request('POST',f"/coverage/claims/{ci['id']}/settlements",{'amount':300,'referenceNo':'QA-INSURANCE-2','receivedOn':str(date.today())},insurer)
assert settled['status']=='SETTLED' and settled['patientPayable']==300
# Overnight duty, overlap checks and bank-reference payroll reconciliation.
request('POST','/staffing/roster',{'employeeId':2,'shiftDate':str(date.today()+timedelta(days=6)),'startsAt':'22:00','endsAt':'06:00'},hr)
request('POST','/staffing/roster',{'employeeId':2,'shiftDate':str(date.today()+timedelta(days=7)),'startsAt':'01:00','endsAt':'03:00'},hr,409)
payroll=next(x for x in request('GET','/staffing/payroll',token=hr) if x['employee_id']==1)
request('POST',f"/staffing/salary/{payroll['id']}/reconcile",{'referenceNo':'QA-SALARY-VERIFIED'},hr)
assert request('GET','/hr/me/salary',token=doctor)[0]['payment_status']=='PAID'
# Consultation signing captures the fee and completion follows actual payment.
request('POST',f"/workflow/encounters/{visit['id']}/billing",{},doctor)
assert request('GET',f"/encounters/{visit['id']}",token=doctor)['status']=='BILLING'
for bill in request('GET','/finance/invoices',token=a):
    if bill['encounter_id']==visit['id'] and bill['status']!='PAID':
        detail_bill=request('GET',f"/finance/invoices/{bill['id']}",token=a)
        if detail_bill['patientOutstanding']>0:request('POST',f"/finance/invoices/{bill['id']}/payments",{'parts':[{'method':'CASH','amount':detail_bill['patientOutstanding']}]},a)
assert request('GET',f"/encounters/{visit['id']}",token=doctor)['status']=='COMPLETED'
request('POST','/operations/lab/orders',{'encounterId':visit['id'],'tests':[{'testCode':'CBC'}]},doctor,409)
# New doctor roster rows create public slots; absence queues existing bookings.
roster_day=date.today()+timedelta(days=30)
request('POST','/staffing/roster',{'employeeId':1,'shiftDate':str(roster_day),'startsAt':'09:00','endsAt':'10:00'},hr)
new_slots=[x for x in request('GET','/public/slots') if str(x['starts_at']).startswith(str(roster_day)) and x['doctor_employee_id']==1]
assert len(new_slots)==2
new_booking=request('POST','/scheduling/book',{'patientId':p['id'],'slotId':new_slots[0]['id']},a)
original_shift=next(x for x in request('GET','/staffing/roster',token=hr) if x['employee_id']==1 and str(x['shift_date'])==str(roster_day))
request('POST',f"/enterprise/roster/{original_shift['id']}/absent",{},hr)
assert not any(x['id']==new_slots[1]['id'] for x in request('GET','/public/slots'))
assert any(x['appointment_id']==new_booking['id'] and x['status']=='PENDING' for x in request('GET','/scheduling/requests',token=a))
backup=request('POST','/employees',{'name':'Dr Synthetic Backup','department':'DEMO','designation':'DOCTOR','mobile':'0000000010'},hr)
request('POST','/staff',{'username':'demo.backupdoctor','password':DEMO,'role':'DOCTOR','employeeId':backup['id']},hr)
request('POST','/staffing/requests',{'rosterId':original_shift['id'],'replacementEmployeeId':backup['id'],'reason':'Synthetic sudden absence coverage'},hr)
swap=next(x for x in request('GET','/staffing/requests',token=hr) if x['roster_id']==original_shift['id'])
request('POST',f"/staffing/requests/{swap['id']}/review",{'status':'APPROVED'},hr)
assert request('GET',f"/appointments/{new_booking['id']}",token=a)['doctorEmployeeId']==backup['id']
assert any(x['id']==new_slots[1]['id'] and x['doctor_employee_id']==backup['id'] for x in request('GET','/public/slots'))
# Returning patient access preserves the MRN and creates no registration charge.
request('POST','/scheduling/patient-access',{'patientId':p['id'],'username':'demo.returning','password':DEMO,'identityVerified':True},a)
returning=login('demo.returning',DEMO)
assert request('GET','/patient/profile',token=returning)['uhid']==p['uhid']
request('PUT','/patient/profile',{'mobile':'0000000011','address':'Synthetic updated address','bloodGroup':'O+'},returning)
assert request('GET','/patient/profile',token=returning)['mobile']=='0000000011'
# Verified electronic references cannot be recorded twice against different bills.
duplicate_bill=request('POST','/operations/billing/invoices',{'patientId':p['id'],'items':[{'description':'Synthetic duplicate-reference check','qty':1,'unitPrice':100}]},a)
request('POST',f"/finance/invoices/{duplicate_bill['id']}/payments",{'parts':[{'method':'UPI','amount':10,'referenceNo':'SYNTHETIC-OK','verified':True}]},a,409)
# Patient-linked consumable returns preserve batch history and create a credit queue item.
consumption=next(x for x in request('GET','/pharmacy/patient-consumables',token=nurse) if x['patient_id']==p['id'])
request('POST','/pharmacy/consumable-returns',{'movementId':consumption['id'],'quantity':1,'reason':'Synthetic unused consumable'},nurse)
request('POST','/pharmacy/consumable-returns',{'movementId':consumption['id'],'quantity':1,'reason':'Invalid duplicate return'},nurse,409)
# Disabled employee credentials stop working while HR history remains.
request('PUT',f"/employees/{backup['id']}",{'name':'Dr Synthetic Backup','department':'DEMO','designation':'DOCTOR','status':'INACTIVE'},hr)
request('POST','/auth/login',{'username':'demo.backupdoctor','password':DEMO},expected=401)
# Department heads can approve their own department without payroll access.
request('POST','/security/roles',{'name':'SYNTHETIC_HEAD','label':'Synthetic department head','permissions':['hr.approve','roster.approve']},a)
request('POST','/staff',{'username':'demo.head','password':DEMO,'role':'SYNTHETIC_HEAD','employeeId':2},hr)
head=login('demo.head',DEMO)
request('POST','/staffing/heads',{'department':'DEMO','username':'demo.head'},hr)
head_leave_day=date.today()+timedelta(days=45)
request('POST','/hr/me/leaves',{'leaveType':'CASUAL','startDate':str(head_leave_day),'endDate':str(head_leave_day),'reason':'Synthetic department approval'},doctor)
head_leave=next(x for x in request('GET','/hr/admin/leaves',token=head) if x['reason']=='Synthetic department approval')
request('POST',f"/hr/admin/leaves/{head_leave['id']}/review",{'status':'APPROVED'},hr,403)
request('POST',f"/hr/admin/leaves/{head_leave['id']}/review",{'status':'APPROVED'},head)
request('GET','/staffing/payroll',token=head,expected=403)
assert request('GET','/notifications',token=head)
# Cancellation releases its slot and rejects further self-rescheduling.
cancel_slot=request('GET','/public/slots')[0]
cancel_appt=request('POST','/patient/book',{'slotId':cancel_slot['id'],'reason':'Synthetic cancellation'},t)
request('POST',f"/patient/appointments/{cancel_appt['id']}/cancel",{'reason':'Synthetic patient cancellation'},t)
assert any(x['id']==cancel_slot['id'] for x in request('GET','/public/slots'))
request('POST',f"/patient/appointments/{cancel_appt['id']}/reschedule",{'slotId':cancel_slot['id']},t,409)
# Outsourced laboratory workflow tracks dispatch/receipt before verified results.
walk=request('POST','/workflow/walk-in',{'patientId':appointment['patient_id'],'doctorEmployeeId':1,'chiefComplaint':'Synthetic outsource workflow'},a)
request('POST',f"/workflow/encounters/{walk['id']}/vitals",{'pulse':70,'spo2':99},nurse)
request('POST',f"/workflow/encounters/{walk['id']}/start-consultation",{},doctor)
request('POST',f"/workflow/encounters/{walk['id']}/consultation",{'diagnosis':'Synthetic testing only'},doctor)
external=request('GET','/portal/options',token=labtech)['externalLabs'][0]
outsourced=request('POST','/operations/lab/orders',{'encounterId':walk['id'],'tests':[{'testCode':'VITD','processingType':'OUTSOURCE','externalLabId':external['id']}]},doctor)
out_item=next(x for x in request('GET','/operations/lab/items',token=labtech) if x['lab_order_id']==outsourced['id'])
out_bill=request('GET',f"/finance/invoices/{out_item['invoice_id']}",token=a)
request('POST',f"/finance/invoices/{out_bill['id']}/payments",{'parts':[{'method':'CASH','amount':out_bill['patientOutstanding']}]},a)
for status in ['BILLED','SAMPLE_COLLECTED']:out_item=request('POST',f"/operations/lab/items/{out_item['id']}/transition",{'status':status},labtech)
request('POST',f"/operations/lab/items/{out_item['id']}/transition",{'status':'IN_PROCESS'},labtech,409)
request('POST','/operations/lab/dispatches',{'labOrderItemId':out_item['id'],'sampleId':out_item['sample_barcode'],'courierDetails':'Synthetic courier','trackingNumber':'QA-TRACK'},labtech)
dispatch=next(x for x in request('GET','/operations/lab/dispatches',token=labtech) if x['lab_order_item_id']==out_item['id'])
request('POST',f"/operations/lab/dispatches/{dispatch['id']}/receive",{},labtech)
request('POST',f"/operations/lab/items/{out_item['id']}/transition",{'status':'IN_PROCESS'},labtech)
request('POST',f"/operations/lab/items/{out_item['id']}/transition",{'status':'RESULT_ENTERED','resultValue':'Synthetic result','resultUnit':'DEMO','referenceRange':'Synthetic range'},labtech)
request('POST',f"/operations/lab/items/{out_item['id']}/transition",{'status':'VERIFIED','verifiedBy':'Synthetic'},pathologist)
request('POST',f"/operations/lab/items/{out_item['id']}/transition",{'status':'COMPLETED'},supervisor)
assert request('GET',f"/lab-orders/{outsourced['id']}",token=labtech)['status']=='COMPLETED'
# General-to-ICU transfer retains the case and captures nonzero bed-rate segments.
request('POST','/inpatient/beds',{'ward':'QA-GENERAL','bedNo':'QA-G1','floorName':'2','bedType':'GENERAL','dailyRate':1440},a)
request('POST','/inpatient/beds',{'ward':'QA-ICU','bedNo':'QA-I1','floorName':'2','bedType':'ICU','dailyRate':2880},a)
qa_beds=request('GET','/inpatient/beds',token=a)
qa_general=next(x for x in qa_beds if x['bed_no']=='QA-G1');qa_icu=next(x for x in qa_beds if x['bed_no']=='QA-I1')
qa_admit=request('POST','/operations/ipd/admit',{'patientId':appointment['patient_id'],'encounterId':walk['id'],'ward':qa_general['ward'],'bedNo':qa_general['bed_no']},a)
request('POST','/operations/ipd/admit',{'patientId':appointment['patient_id'],'ward':qa_icu['ward'],'bedNo':qa_icu['bed_no']},a,409)
qa_case=request('POST','/enterprise/claims',{'admissionId':qa_admit['id'],'provider':'Synthetic retained ICU case','policyNo':'QA-ICU','claimType':'CASHLESS','claimAmount':100,'nonPayable':0},insurer)
request('POST',f"/ward/admissions/{qa_admit['id']}/transfer",{'bedId':qa_icu['id']},a)
assert request('GET',f"/coverage/claims/{qa_case['id']}",token=insurer)['admission_id']==qa_admit['id']
request('POST',f"/operations/ipd/admissions/{qa_admit['id']}/discharge",{},a)
bed_bills=[x for x in request('GET','/finance/invoices',token=a) if x['encounter_id']==walk['id']]
bed_lines=[]
for bill in bed_bills:bed_lines += [x for x in request('GET',f"/finance/invoices/{bill['id']}",token=a)['items'] if x['item_type']=='BED_STAY']
assert len(bed_lines)==2 and sum(x['amount'] for x in bed_lines)>=3
# Ward returns/waste/damage maintain stock ledgers, without inventing a patient charge.
for action in ['WASTE','DAMAGE']:request('POST',f"/pharmacy/batches/{early['id']}/action?action="+action,{'quantity':1,'reason':'Synthetic '+action},pharmacist)
request('POST',f"/pharmacy/ward-stock/{early['id']}/action?action=RETURN",{'quantity':1,'ward':ad['ward'],'reason':'Synthetic unused ward return'},nurse)
cons_credit=next(x for x in request('GET','/finance/return-credits',token=a) if x['dispense_id'] is None and x['reason']=='Synthetic unused consumable')
request('POST',f"/finance/return-credits/{cons_credit['id']}/reconcile",{},a)
# Department attribution follows the doctor for care and service departments for charges.
report_month=str(date.today())[:7]
lab_metrics=request('GET','/reports/overview?month='+report_month+'&department=Laboratory',token=a)
assert lab_metrics['current']['collections']>=450 and lab_metrics['current']['invoicedRevenue']>=450
clinical_metrics=request('GET','/reports/overview?month='+report_month+'&department=DEMO',token=a)
assert clinical_metrics['current']['visits']>=1 and clinical_metrics['current']['averageWaitMinutes']>=0
assert clinical_metrics['current']['collections']>=500
# XLSX is a real OpenXML workbook; management PDF is a real PDF.
import zipfile,io
for ext in ['xlsx','pdf']:
    req=urllib.request.Request(BASE+'/reports/export.'+ext+'?month='+str(date.today())[:7],headers={'Authorization':'Bearer '+a})
    with urllib.request.urlopen(req) as response:data=response.read()
    if ext=='xlsx':assert 'xl/workbook.xml' in zipfile.ZipFile(io.BytesIO(data)).namelist()
    else:assert data.startswith(b'%PDF-')
    checks.append('Management '+ext+' export: valid document structure')
# Audit and notification records exist; sign-out revokes existing JWT.
assert request('GET','/security/audit',token=a)
assert request('GET','/notifications',token=t)
request('POST','/auth/logout',{},viewer)
request('GET','/finance/invoices',token=viewer,expected=401)
Path('enterprise-smoke-result.json').write_text(json.dumps({'passed':len(checks),'checks':checks},indent=2))
print('Extended enterprise checks passed:',len(checks))
