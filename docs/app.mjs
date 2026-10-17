import {Hospital,money} from './model.mjs';
const $=s=>document.querySelector(s);
const status=$('#status');
function message(text,error=false){status.textContent=text;status.className=error?'error':'';status.hidden=false;}
let hospital;
try{hospital=new Hospital(localStorage);}catch(e){message('Could not open device storage. Demo paused: '+e.message,true);document.querySelectorAll('form button,#sample,#export,#reset').forEach(x=>x.disabled=true);}
const settings={patients:['Patient registration','Register a patient to connect appointments, notes, and bills.'],appointments:['Appointment scheduling','Book a patient visit and check for conflicting appointment slots.'],notes:['Medical notes','Keep fictional notes linked to the correct patient.'],bills:['Billing & payment status','Create bills and record whether they have been paid.'],records:['Administrative records','Review or export all records saved on this device.']};
function show(screen){document.querySelectorAll('.screen').forEach(x=>x.hidden=x.id!==screen);document.querySelectorAll('[data-screen]').forEach(x=>{if(x.dataset.screen===screen)x.setAttribute('aria-current','page');else x.removeAttribute('aria-current');});$('#screen-title').textContent=settings[screen][0];$('#screen-description').textContent=settings[screen][1];status.hidden=true;}
document.querySelectorAll('[data-screen]').forEach(x=>x.addEventListener('click',()=>show(x.dataset.screen)));
function table(target,headers,rows){
 target.replaceChildren();const table=document.createElement('table');const head=document.createElement('thead');const hr=document.createElement('tr');
 headers.forEach(text=>{const th=document.createElement('th');th.scope='col';th.textContent=text;hr.append(th);});head.append(hr);table.append(head);
 const body=document.createElement('tbody');
 if(!rows.length){const row=document.createElement('tr');const cell=document.createElement('td');cell.colSpan=headers.length;cell.className='empty';cell.textContent='No records yet. Add a record above or load sample records.';row.append(cell);body.append(row);}
 rows.forEach(values=>{const row=document.createElement('tr');values.forEach(value=>{const cell=document.createElement('td');if(value instanceof Node)cell.append(value);else cell.textContent=String(value);row.append(cell);});body.append(row);});table.append(body);target.append(table);
}
function billRows(actions){return hospital.data.bills.map(b=>{const badge=document.createElement('span');badge.className='badge'+(b.paid?' paid':'');badge.textContent=b.paid?'PAID':'UNPAID';const row=[b.id,b.patientId,b.description,money(b.cents),badge];if(actions){const action=document.createElement('span');if(!b.paid){const button=document.createElement('button');button.className='pay-button';button.textContent='Mark paid';button.setAttribute('aria-label',`Mark bill ${b.id} paid`);button.addEventListener('click',()=>run(()=>{hospital.markPaid(b.id);return `Bill ${b.id} marked PAID. This does not transfer money.`;}));action.append(button);}else action.textContent='Completed';row.push(action);}return row;});}
function render(){
 if(!hospital)return;
 const d=hospital.data;for(const key of ['patients','appointments','notes'])$('#count-'+key).textContent=d[key].length;$('#count-bills').textContent=d.bills.filter(x=>!x.paid).length;
 document.querySelectorAll('.patient-select').forEach(select=>{const old=select.value;select.replaceChildren();const placeholder=new Option(d.patients.length?'Select patient':'Register a patient first','');select.add(placeholder);d.patients.forEach(p=>select.add(new Option(`${p.id} — ${p.name}`,p.id)));if(d.patients.some(p=>String(p.id)===old))select.value=old;});
 const config=[['Patients',['Patient ID','Patient name','Age','Contact number'],d.patients.map(p=>[p.id,p.name,p.age,p.phone])],['Appointments',['ID','Patient ID','Doctor','Date','Time'],d.appointments.map(a=>[a.id,a.patientId,a.doctor,a.date,a.time])],['Medical notes',['Note ID','Patient ID','Note'],d.notes.map(n=>[n.id,n.patientId,n.note])]];
 ['patient','appointment','note'].forEach((key,i)=>table($('#'+key+'-table'),config[i][1],config[i][2]));table($('#bill-table'),['Bill ID','Patient ID','Description','Amount','Status','Action'],billRows(true));
 const all=$('#record-tables');all.replaceChildren();for(const [title,headers,rows] of [...config,['Bills',['Bill ID','Patient ID','Description','Amount','Status'],billRows(false)]]){const h=document.createElement('h4');h.textContent=title;const wrap=document.createElement('div');wrap.className='table-wrap';table(wrap,headers,rows);all.append(h,wrap);}
 $('#sample').disabled=Object.values(d).some(x=>x.length);
}
function run(action){if(!hospital)return;try{const result=action();render();message(result);}catch(e){message(e.message,true);}}
function form(id,action){$(id).addEventListener('submit',e=>{e.preventDefault();if(!hospital)return;const data=Object.fromEntries(new FormData(e.currentTarget));run(()=>action(data));});}
form('#patient-form',d=>{const p=hospital.register(d.name,d.age,d.phone);return `Patient registered and saved. Patient ID: ${p.id}`;});
form('#appointment-form',d=>{const a=hospital.book(d.patientId,d.doctor,d.date,d.time);return `Appointment ${a.id} booked and saved.`;});
form('#note-form',d=>{hospital.addNote(d.patientId,d.note);return 'Medical note saved.';});
form('#bill-form',d=>{const b=hospital.bill(d.patientId,d.description,d.amount);return `Bill ${b.id} created with UNPAID status.`;});
$('#sample').addEventListener('click',()=>run(()=>{hospital.sample();return 'Fictional sample records loaded. Try booking an appointment or marking the bill paid.';}));
$('#reset').addEventListener('click',()=>{if(hospital&&confirm('Delete all demo records saved in this browser?'))run(()=>{hospital.clear();return 'Demo records cleared.';});});
$('#export').addEventListener('click',()=>{if(!hospital)return;const url=URL.createObjectURL(new Blob([JSON.stringify(hospital.data,null,2)],{type:'application/json'}));const a=document.createElement('a');a.href=url;a.download='hospital-demo-records.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);});
render();
