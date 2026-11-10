# Arogya hospital workflow guide

## Configure the hospital

Sign in as admin. Set hospital branding, registration fee, consultation fee,
appointment duration, rescheduling cutoff and early medication window in Hospital
settings. Create hospital departments first, then employees before logins; employee and profile IDs are generated.
Select records by their labels rather than entering linkage IDs. Add doctor
profiles, wards/beds/rates, laboratory tests, suppliers and stock catalogue items.
Create custom roles by selecting individual permissions. Protected administrator
permissions cannot be delegated to a custom role; system roles are immutable.

Assign department heads using HR. A custom head needs `hr.approve` and/or
`roster.approve`, not payroll access. HR provides approval fallback until heads
are configured; administrators can override departmental restrictions.

## Reception and patient self service

1. Search the patient directory before registration. New staff-registered patients
   receive a sequential immutable MRN and a one-time configurable registration bill.
2. Returning patients keep their existing MRN. Reception can restore portal access
   after verifying identity. Do not create a second patient to restore access.
3. New self-service patients register and complete the simulated online registration
   fee before confirming a booking. That simulation never enters hospital revenue.
4. Publish doctor duty through the central roster. Doctor shifts generate eligible
   public slots. Overlapping shifts and approved leave are checked; absences block
   slots and queue booked patients for reception assistance.
5. Book an available slot. Reservation is transactional and unique. A patient can
   self-reschedule before the configured cutoff, cancel with a reason, or ask
   reception for assistance. Reception can create a provisional PENDING appointment
   and resolve it later to an eligible slot.
6. Assign the nurse and additional clinicians through Care assignments. Appointment
   doctors are linked automatically using employee IDs. Doctors and nurses see
   only patients assigned to their care team.

## OPD

Check in a booked appointment or record a walk-in with a selected doctor.
The nurse records vitals; the patient moves to WAITING. The doctor starts a
consultation, saves notes and orders laboratory/radiology or a prescription.
Prescription drafts are not issued to patients. Finalization issues an immutable
PDF snapshot with MRN, clinician, timestamp, branding and verification QR.

Sign and send the visit to billing. The consultation fee is captured once. A visit
in BILLING completes after its linked invoices are paid. Laboratory charges are
captured from the catalogue when ordered; sample collection cannot advance past
billing until the linked invoice is paid.

## Laboratory and radiology

Laboratory: ORDERED → BILLED → SAMPLE_COLLECTED → IN_PROCESS → RESULT_ENTERED →
VERIFIED → COMPLETED. Technician processes early stages, pathologist verifies,
and supervisor completes. Each collected item receives a sample barcode. For
outsource tests, select an external lab, dispatch with specimen/courier/tracking,
then confirm receipt before processing. Units and reference ranges can be stored
in the test catalogue; result entry and verification are explicit steps.

Verification automatically issues the patient report PDF. Parent order status
rolls up from its items. Radiology follows ORDERED → SCHEDULED → IN_PROCESS →
REPORTED → VERIFIED and similarly issues only verified reports.

QR verification confirms the issued-record metadata. It is not a cryptographic
digital signature or a substitute for comparing the issued document in the portal.

## IPD / ICU and nursing

The doctor recommends admission; admission staff select an available or
patient-reserved bed. A patient can have only one active admission and each bed
only one occupant. Bed states include AVAILABLE, RESERVED, OCCUPIED, CLEANING,
MAINTENANCE and BLOCKED. The board filters ward/floor/type/status and shows counts.

Transfers retain the admission and insurance case. Bed-segment rates are captured
when allocated; bed charges are prorated by elapsed minutes with a minimum one
minute per segment. Transfers/discharge close segments and record movements.
Discharged beds require cleaning before reuse. Configure rates before use.

Doctors prescribe a medication schedule with route, dose, interval and dose count.
A pharmacist verifies the order, then dispenses the patient-linked stock. Nurses
can record GIVEN only after verification, sufficient dispensing and within the
configured early administration window. HELD/REFUSED/MISSED require a reason;
completed outcomes cannot be overwritten. Nursing assessment, care plan and shift
handover notes are separate records.

## Pharmacy, inventory and consumables

Create a purchase order, then receive goods against its lines with batch, expiry,
unit cost and sale price. Receipts cannot exceed the ordered quantity. Patient
prescription/IPD dispensing uses locked first-expiring, first-out batches and
rejects expired or insufficient stock. Charges are captured to the patient's bill.

Returns cannot exceed unused supplied quantities; administered doses cannot be
returned. Billing receives a return-credit request. For a paid return, staff must
verify the actual refund reference before applying its credit. An insured return
that exceeds patient refundable money is blocked until insurer reconciliation.

Transfer batches to ward stock. Nurses record patient-linked consumables, returns,
ward waste/damage and unused ward returns. Movements retain batch, patient/ward,
quantity, reason and actor. Patient usage creates charges; returns create credits.

## Billing and insurance

Use itemized invoices. Enter each Cash/Card/UPI/Bank component separately in the
split-payment editor. Electronic payments require a verified reference; references
cannot be recorded twice. Each receipt remains a separate transaction. Refunds,
discounts, credit notes and tax adjustments record reason and actor. Patient
refunds cannot return money received from the insurer.

For cashless insurance, open an admission-linked case, allocate eligible and
nonpayable lines, obtain preauthorization, request/review enhancements and submit
final claims. Rejected enhancements preserve the previous authorization. Link the
approved insurer share to the patient's invoices before settlement. Patient
payments cannot collect that reserved share. Insurer receipts are reconciled in
parts against unique references and posted to linked invoices. Reimbursement
settlements record claim status but do not create hospital receipts.

The case shows eligible amount, patient payable, insurer outstanding and settlement
difference. Record insurer rejection with its reason; unsettled authorizations are
released back to patient responsibility. Transfers do not create a new claim case.

## Staffing, HR, helpdesk and management

Publish shifts including overnight duties; overlapping duties and leave conflicts
are blocked. A replacement must match department and professional designation.
Head approval replaces the duty and updates eligible doctor availability/booked
appointments, retaining an audit trail and notifications. Record attendance,
publish basic/allowance/deduction payslips and mark payroll paid only after bank
reference reconciliation. The application does not transfer salaries.

Users can open helpdesk tickets and follow conversations. Authorized helpdesk
staff route, assign, escalate and resolve tickets. Due times reflect priority.
Patients see their own tickets only. Notifications are delivered inside the portal.

Management analytics compares month-over-month patient/visit volumes, waiting,
invoiced amounts, collections/refunds, admissions and recorded quality events.
Filter by month and department, then export a real XLSX workbook or PDF report.
Collections include actual cashless receipts and exclude dummy registration fees. Diagnostic, pharmacy, bed and registration charges are attributed to their service departments; consultations follow the doctor's department. Select a billing department when entering a standalone manual invoice.
