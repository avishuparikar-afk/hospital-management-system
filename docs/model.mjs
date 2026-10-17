export const STORAGE_KEY='hospital-demo-v1';
const empty=()=>({patients:[],appointments:[],notes:[],bills:[]});
const required=(s,label)=>{if(typeof s!=='string'||!s.trim())throw Error(`${label} is required.`);return s.trim();};
export const money=cents=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR'}).format(cents/100);
export class Hospital {
 constructor(storage){
  this.storage=storage;
  const raw=storage.getItem(STORAGE_KEY);
  this.data=raw?JSON.parse(raw):empty();
  for(const k of Object.keys(empty()))if(!Array.isArray(this.data[k]))throw Error('Saved demo data is invalid.');
  for(const list of Object.values(this.data))for(const row of list)if(!Number.isSafeInteger(row.id)||row.id<1)throw Error('Invalid saved record ID.');
 }
 commit(next){this.storage.setItem(STORAGE_KEY,JSON.stringify(next));this.data=next;}
 add(type,record){const next=JSON.parse(JSON.stringify(this.data));record.id=Math.max(0,...next[type].map(x=>x.id))+1;next[type].push(record);this.commit(next);return record;}
 patient(id){id=Number(id);if(!this.data.patients.some(x=>x.id===id))throw Error('Select a registered patient.');return id;}
 register(name,age,phone){
  name=required(name,'Patient name');phone=required(phone,'Contact number');
  if(!/^\d+$/.test(String(age)))throw Error('Age must be a whole number.');age=Number(age);
  if(age<1||age>120)throw Error('Age must be between 1 and 120.');
  if(!/^\+?\d{10,15}$/.test(phone))throw Error('Contact needs 10 to 15 digits, optionally starting with +.');
  return this.add('patients',{name,age,phone});
 }
 book(patientId,doctor,date,time){
  patientId=this.patient(patientId);doctor=required(doctor,'Doctor');
  if(!/^\d{4}-\d{2}-\d{2}$/.test(date))throw Error('Enter a valid appointment date.');
  const [y,m,d]=date.split('-').map(Number);const dt=new Date(Date.UTC(y,m-1,d));
  if(y<1900||dt.getUTCFullYear()!==y||dt.getUTCMonth()!==m-1||dt.getUTCDate()!==d)throw Error('Enter a valid calendar date.');
  if(!/^\d{2}:\d{2}$/.test(time)||Number(time.slice(0,2))>23||Number(time.slice(3))>59)throw Error('Enter a valid time HH:mm.');
  if(this.data.appointments.some(a=>a.date===date&&a.time===time&&(a.patientId===patientId||a.doctor.toLowerCase()===doctor.toLowerCase())))throw Error('Doctor or patient already has that appointment slot.');
  return this.add('appointments',{patientId,doctor,date,time});
 }
 addNote(patientId,note){return this.add('notes',{patientId:this.patient(patientId),note:required(note,'Medical note')});}
 bill(patientId,description,value){
  patientId=this.patient(patientId);description=required(description,'Description');value=String(value).trim();
  if(!/^\d+(?:\.\d{1,2})?$/.test(value))throw Error('Enter a positive amount with at most two decimal places.');
  const [whole,fraction='']=value.split('.');const cents=Number(whole)*100+Number(fraction.padEnd(2,'0'));
  if(!Number.isSafeInteger(cents)||cents<=0||cents>999999999999)throw Error('Amount must be between INR 0.01 and INR 9,999,999,999.99.');
  return this.add('bills',{patientId,description,cents,paid:false});
 }
 markPaid(id){const next=JSON.parse(JSON.stringify(this.data));const b=next.bills.find(x=>x.id===Number(id));if(!b)throw Error('Bill not found.');b.paid=true;this.commit(next);}
 sample(){
  if(Object.values(this.data).some(a=>a.length))throw Error('Sample data can be loaded only into an empty demo.');
  this.commit({patients:[{id:1,name:'Arun Demo',age:25,phone:'9000000000'},{id:2,name:'Riya Demo',age:30,phone:'9000000001'}],appointments:[{id:1,patientId:1,doctor:'Mehta',date:'2026-10-20',time:'10:30'}],notes:[{id:1,patientId:1,note:'Routine check-up — fictional demonstration note.'}],bills:[{id:1,patientId:1,description:'Consultation',cents:50000,paid:false}]});
 }
 clear(){this.commit(empty());}
}
