import React,{useEffect,useMemo,useState} from 'react'
import {createRoot} from 'react-dom/client'
import {Search,BookOpen,LogOut,LayoutDashboard,Library,Receipt,ShieldCheck,ArrowLeft,RotateCcw,Plus,Trash2,Edit3,Clock3,AlertCircle,CheckCircle2,RefreshCw} from 'lucide-react'
import './styles.css'

const API=import.meta.env.VITE_API_URL ?? ''
const sleep=ms=>new Promise(resolve=>setTimeout(resolve,ms))
const RETRYABLE_STATUS=new Set([502,503,504])
const api=async(path,options={})=>{
 const maxAttempts=path.startsWith('/api/auth/')?8:10
 let lastError=new Error('Request failed. Please try again.')
 for(let attempt=1;attempt<=maxAttempts;attempt++){
  const isPublicAuthEndpoint=path==='/api/auth/login'||path==='/api/auth/register'||path==='/api/auth/health'
  const token=isPublicAuthEndpoint?null:localStorage.getItem('archvialia_token')
  const headers={'Content-Type':'application/json',...(options.headers||{})}
  if(token)headers.Authorization=`Bearer ${token}`
  let res
  try{
   res=await fetch(`${API}${path}`,{...options,headers})
  }catch(e){
   lastError=new Error('Cannot reach the library server. Please check that Archvialia is running.')
   if(attempt===maxAttempts)throw lastError
   await sleep(Math.min(800*attempt,2200))
   continue
  }
  if(res.ok)return res.status===204?null:res.json()
  let msg=`Request failed (${res.status})`
  try{
   const text=await res.text()
   if(text){
    try{
     const d=JSON.parse(text)
     msg=d.message||d.detail||d.error||d.title||msg
    }catch{
     if(text.length<240)msg=text
    }
   }
  }catch{}
  if(!RETRYABLE_STATUS.has(res.status))throw new Error(msg)
  lastError=new Error(msg)
  if(attempt===maxAttempts)throw lastError
  await sleep(Math.min(800*attempt,2200))
 }
 throw lastError
}

function readStoredUser(){
 try{
  const raw=localStorage.getItem('archvialia_user')
  return raw?JSON.parse(raw):null
 }catch{
  localStorage.removeItem('archvialia_user')
  return null
 }
}
function App(){
 const [user,setUser]=useState(readStoredUser)
 const [page,setPage]=useState('dashboard')
 const [books,setBooks]=useState([]); const [borrows,setBorrows]=useState([]); const [fines,setFines]=useState([]); const [adminBorrows,setAdminBorrows]=useState([]); const [adminFines,setAdminFines]=useState([]); const [adminUsers,setAdminUsers]=useState([]); const [query,setQuery]=useState(''); const [loading,setLoading]=useState(false); const [toast,setToast]=useState('')
 const isAdmin=user?.role==='ADMIN'
 const notify=m=>{setToast(m);setTimeout(()=>setToast(''),2800)}
 const loadAdminData=async()=>{
  if(!user||user.role!=='ADMIN')return
  const results=await Promise.allSettled([
   api('/api/borrow/admin/all'),
   api('/api/fines/admin/all'),
   api('/api/auth/admin/users')
  ])
  const [borrowsResult,finesResult,usersResult]=results
  if(borrowsResult.status==='fulfilled') setAdminBorrows(borrowsResult.value)
  else console.warn('Admin circulation refresh failed:',borrowsResult.reason?.message||borrowsResult.reason)
  if(finesResult.status==='fulfilled') setAdminFines(finesResult.value)
  else console.warn('Admin fines refresh failed:',finesResult.reason?.message||finesResult.reason)
  if(usersResult.status==='fulfilled') setAdminUsers(usersResult.value)
  else console.warn('Admin users refresh failed:',usersResult.reason?.message||usersResult.reason)
 }
 const load=async()=>{if(!user)return;setLoading(true);try{const [b,br,f]=await Promise.all([api('/api/books'),api('/api/borrow/me'),api('/api/fines/me')]);setBooks(b);setBorrows(br);setFines(f);if(user.role==='ADMIN')await loadAdminData()}catch(e){notify(e.message)}finally{setLoading(false)}}
 useEffect(()=>{load()},[user])
 useEffect(()=>{if(!user||user.role!=='ADMIN'||page!=='admin')return;loadAdminData();const timer=setInterval(loadAdminData,5000);return()=>clearInterval(timer)},[user?.userId,user?.role,page])
 const filtered=useMemo(()=>books.filter(b=>`${b.title} ${b.author} ${b.category}`.toLowerCase().includes(query.toLowerCase())),[books,query])
 const handleLogin=u=>{
  localStorage.setItem('archvialia_token',u.token)
  localStorage.setItem('archvialia_user',JSON.stringify(u))
  setBooks([]);setBorrows([]);setFines([]);setAdminBorrows([]);setAdminFines([]);setAdminUsers([])
  setPage('dashboard');setUser(u)
 }
 if(!user)return <Auth key="signed-out" onLogin={handleLogin}/>
 const logout=()=>{
  localStorage.removeItem('archvialia_token')
  localStorage.removeItem('archvialia_user')
  setBooks([]);setBorrows([]);setFines([]);setAdminBorrows([]);setAdminFines([]);setAdminUsers([])
  setPage('dashboard');setToast('');setUser(null)
 }
 const borrow=async bookId=>{try{await api('/api/borrow',{method:'POST',body:JSON.stringify({bookId})});notify('Book borrowed successfully');await load()}catch(e){notify(e.message)}}
 const ret=async id=>{try{await api(`/api/borrow/${id}/return`,{method:'PUT'});notify('Book returned successfully');await load()}catch(e){notify(e.message)}}
 const bookPayload=data=>({...data,totalCopies:Number(data.totalCopies),availableCopies:Number(data.availableCopies),ebookUrl:data.ebookUrl?.trim()||null})
 const createBook=async data=>{try{await api('/api/books',{method:'POST',body:JSON.stringify(bookPayload(data))});notify('Book added');await load()}catch(e){notify(e.message)}}
 const updateBook=async(id,data)=>{try{await api(`/api/books/${id}`,{method:'PUT',body:JSON.stringify(bookPayload(data))});notify('Book updated');await load()}catch(e){notify(e.message)}}
 const removeBook=async id=>{if(!confirm('Remove this book from the catalogue?'))return;try{await api(`/api/books/${id}`,{method:'DELETE'});notify('Book removed');await load()}catch(e){notify(e.message)}}
 const changeRole=async(id,role)=>{try{await api(`/api/auth/admin/users/${id}/role`,{method:'PUT',body:JSON.stringify({role})});notify('User role updated');await load()}catch(e){notify(e.message)}}
 return <div className="app"><aside className="sidebar"><div className="brand"><div className="brandMark">A</div><div><strong>Archvialia</strong><span>Digital Library</span></div></div><nav>
  <button className={page==='dashboard'?'active':''} onClick={()=>setPage('dashboard')}><LayoutDashboard/>Dashboard</button>
  <button className={page==='library'?'active':''} onClick={()=>setPage('library')}><Library/>Library</button>
  <button className={page==='borrowings'?'active':''} onClick={()=>setPage('borrowings')}><BookOpen/>My Borrowings</button>
  <button className={page==='fines'?'active':''} onClick={()=>setPage('fines')}><Receipt/>My Fines</button>
  {isAdmin&&<button className={page==='admin'?'active':''} onClick={()=>setPage('admin')}><ShieldCheck/>Admin</button>}
 </nav><div className="sideBottom"><div className="miniUser"><div className="avatar">{user.name?.[0]||'U'}</div><div><strong>{user.name}</strong><span>{user.role}</span></div></div><button className="logout" onClick={logout}><LogOut/>Sign out</button></div></aside>
 <main className="main"><header className="topbar"><div className="mobileBrand">Archvialia</div><div className="topSearch"><Search/><input value={query} onChange={e=>{setQuery(e.target.value);setPage('library')}} placeholder="Search books, authors, categories..."/></div><div className="status"><span className="dot"/> System operational</div></header>
 {page==='dashboard'&&<Dashboard user={user} books={books} borrows={borrows} fines={fines} go={setPage}/>} 
 {page==='library'&&<LibraryPage books={filtered} query={query} setQuery={setQuery} borrow={borrow}/>} 
 {page==='borrowings'&&<Borrowings borrows={borrows} books={books} ret={ret}/>} 
 {page==='fines'&&<Fines fines={fines}/>} 
 {page==='admin'&&isAdmin&&<Admin books={books} createBook={createBook} updateBook={updateBook} removeBook={removeBook} adminBorrows={adminBorrows} adminFines={adminFines} adminUsers={adminUsers} changeRole={changeRole} ret={ret} refreshAdmin={loadAdminData}/>} 
 </main>{toast&&<div className="toast">{toast}</div>}{loading&&<div className="loadingBar"/>}</div>
}

function LibraryScene(){
 const books=['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z']
 return <div className="libraryScene" aria-hidden="true">
  <div className="sceneGlow sceneGlowOne"/><div className="sceneGlow sceneGlowTwo"/>
  <div className="lightBeam beamOne"/><div className="lightBeam beamTwo"/>
  <div className="libraryCeiling"><span/><span/><span/><span/></div>
  <div className="backWall">
   <div className="window"><i/><i/><i/></div>
   <div className="wallClock">ARCHVIALIA</div>
  </div>
  <div className="shelfWall">
   {[0,1,2,3].map(row=><div className="shelfRow" key={row}><div className="books">{books.map((b,i)=><span key={`${row}-${i}`} className={`bookSpine spine${(i+row*3)%8}`}>{b}</span>)}</div><div className="woodShelf"/></div>)}
  </div>
  <div className="libraryFloor"/>
  <div className="readingTable"><div className="tableTop"/><div className="tableLeg legA"/><div className="tableLeg legB"/><div className="lamp"><b/><i/></div><div className="openBook"><span/><span/></div></div>
  <div className="reader readerOne"><div className="head"/><div className="body"/></div>
  <div className="reader readerTwo"><div className="head"/><div className="body"/></div>
  <div className="plant"><i/><i/><i/><b/></div>
  <div className="dust dust1"/><div className="dust dust2"/><div className="dust dust3"/><div className="dust dust4"/>
  <div className="sceneVignette"/>
 </div>
}
function Auth({onLogin}){
 const [mode,setMode]=useState('login')
 const [form,setForm]=useState({name:'',email:'user@archvialia.local',password:'User@12345',role:'STUDENT'})
 const [err,setErr]=useState(''); const [submitting,setSubmitting]=useState(false)
 const switchMode=m=>{setMode(m);setErr('');if(m==='register')setForm(f=>({...f,name:'',email:'',password:'',role:'STUDENT'}));else setForm(f=>({...f,email:'user@archvialia.local',password:'User@12345'}))}
 const submit=async e=>{e.preventDefault();if(submitting)return;setErr('');setSubmitting(true)
  try{
   const payload=mode==='login'?{email:form.email.trim(),password:form.password}: {...form,name:form.name.trim(),email:form.email.trim().toLowerCase()}
   const d=await api(`/api/auth/${mode==='login'?'login':'register'}`,{method:'POST',body:JSON.stringify(payload)})
   onLogin(d)
  }catch(e){setErr(e.message||'Request failed. Please try again.')}finally{setSubmitting(false)}
 }
 return <div className="authPage">
  <div className="authVisual"><LibraryScene/><div className="authShade"/><div className="authCopy">
   <div className="authBrand"><div className="brandMark big">A</div><div><strong>Archvialia</strong><span>E-LIBRARY</span></div></div>
   <div className="eyebrow lightEyebrow">WELCOME TO ARCHVIALIA</div>
   <h1>More Than Books,<br/><em>A Brighter Tomorrow.</em></h1>
   <p>Discover, borrow and explore knowledge in a modern academic library built for students, faculty and lifelong learners.</p>
   <div className="featureRow"><span><BookOpen/> Vast collection</span><span><ShieldCheck/> Secure access</span><span><Clock3/> Always available</span></div>
   <div className="sceneQuote">“A reader today,<br/> a leader tomorrow.”</div>
  </div>
  </div>
  <div className="authPanel"><div className="authCard">
   <div className="authTabs"><button className={mode==='login'?'sel':''} onClick={()=>switchMode('login')}>Sign in</button><button className={mode==='register'?'sel':''} onClick={()=>switchMode('register')}>Create account</button></div>
   <div className="authHeading"><span className="smallMark">A</span><div><h2>{mode==='login'?'Welcome back':'Join Archvialia'}</h2><p className="muted">{mode==='login'?'Continue your learning journey.':'Create a Student or Faculty library account.'}</p></div></div>
   <form onSubmit={submit} noValidate>{mode==='register'&&<label>Name<input autoComplete="name" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} placeholder="Your full name" required/></label>}
    <label>Email<input type="email" autoComplete="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} placeholder="you@university.edu" required/></label>
    <label>Password<input type="password" autoComplete={mode==='login'?'current-password':'new-password'} value={form.password} onChange={e=>setForm({...form,password:e.target.value})} placeholder="At least 8 characters" minLength="8" required/></label>
    {mode==='register'&&<label>Account type<select value={form.role} onChange={e=>setForm({...form,role:e.target.value})}><option value="STUDENT">🎓 Student</option><option value="FACULTY">👨‍🏫 Faculty</option></select></label>}
    {err&&<div className="errorBox"><AlertCircle/><span>{err}</span></div>}
    <button className="primary full authSubmit" disabled={submitting}>{submitting?(mode==='login'?'Signing in…':'Creating account…'):(mode==='login'?'Sign in':'Create account')}<ArrowLeft className="submitArrow"/></button>
   </form>
   <div className="demoHint"><span className="demoTitle">Demo access</span><div><b>Student</b> · user@archvialia.local / User@12345</div><div><b>Librarian</b> · admin@archvialia.local / Admin@12345</div><span>Faculty accounts are created from the Create account tab.</span></div>
  </div><div className="panelFooter">Quiet spaces. Curious minds. Better ideas.</div></div>
 </div>
}
function Dashboard({user,books,borrows,fines,go}){const active=borrows.filter(x=>x.status==='ACTIVE');const available=books.filter(x=>x.availableCopies>0).length;return <section><div className="hero"><div><div className="eyebrow">PERSONAL LIBRARY</div><h1>Good to see you, {user.name.split(' ')[0]}.</h1><p>Discover resources, manage active loans and keep your academic reading on track.</p><button className="primary" onClick={()=>go('library')}>Explore library <ArrowLeft className="flip"/></button></div><div className="heroGraphic"><div className="stackCard c1">Research</div><div className="stackCard c2">Systems</div><div className="stackCard c3">Design</div></div></div><div className="stats"><Stat title="Catalogue" value={books.length} note={`${available} currently available`} icon={<Library/>}/><Stat title="Active loans" value={active.length} note="Keep an eye on due dates" icon={<Clock3/>}/><Stat title="Outstanding fines" value={`₹${fines.reduce((a,f)=>a+Number(f.amount),0).toFixed(2)}`} note={fines.length?'Review your fine history':'All clear'} icon={<Receipt/>}/></div><div className="sectionHead"><div><h2>Recently added</h2><p>Fresh resources in the catalogue</p></div><button className="linkBtn" onClick={()=>go('library')}>View all →</button></div><div className="bookGrid">{books.slice(0,4).map(b=><BookCard key={b.id} b={b} onBorrow={()=>{}} compact/> )}</div></section>}
function Stat({title,value,note,icon}){return <div className="stat"><div className="iconBox">{icon}</div><div><span>{title}</span><strong>{value}</strong><small>{note}</small></div></div>}
function BookCard({b,onBorrow,compact}){return <div className="bookCard"><div className="cover"><span>{b.category?.slice(0,3).toUpperCase()}</span><strong>{b.title}</strong><small>{b.author}</small><i>{b.id}</i></div><div className="bookInfo"><div className="bookTitle">{b.title}</div><div className="bookAuthor">{b.author}</div><div className="bookMeta"><span className={b.availableCopies?'available':'unavailable'}>{b.availableCopies?'Available':'Unavailable'}</span><span>{b.availableCopies}/{b.totalCopies} copies</span></div>{!compact&&<><button className="secondary full" disabled={!b.availableCopies} onClick={onBorrow}>{b.availableCopies?'Borrow resource':'Unavailable'}</button>{b.ebookUrl&&<a className="secondary full ebookLink" href={b.ebookUrl} target="_blank" rel="noreferrer">Read e-book ↗</a>}</>}</div></div>}
function LibraryPage({books,query,setQuery,borrow}){return <section><div className="pageIntro"><div><div className="eyebrow">CATALOGUE</div><h1>Explore the library</h1><p>Search scholarly resources, e-books and digital media.</p></div><div className="searchLarge"><Search/><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search by title, author or category"/></div></div><div className="chipRow"><span>All resources</span><span>Computer Science</span><span>Research</span><span>Design</span><span>Management</span></div><div className="bookGrid">{books.map(b=><BookCard key={b.id} b={b} onBorrow={()=>borrow(b.id)}/>)}</div>{!books.length&&<Empty title="No resources found" text="Try a different search term."/>}</section>}
function Borrowings({borrows,books,ret}){return <section><div className="pageIntro"><div><div className="eyebrow">CIRCULATION</div><h1>My borrowings</h1><p>Track active loans and your complete return history.</p></div></div><div className="loanList">{borrows.map(r=>{const b=books.find(x=>x.id===r.bookId);return <div className="loan"><div className="loanCover">{b?.category?.slice(0,3).toUpperCase()||'BOOK'}</div><div className="loanMain"><strong>{b?.title||`Resource #${r.bookId}`}</strong><span>Borrowed {r.borrowDate}</span><span>Due {r.dueDate}</span></div><div className="loanStatus"><span className={r.status==='ACTIVE'?'pill warn':'pill ok'}>{r.status}</span>{r.status==='ACTIVE'&&<button className="secondary" onClick={()=>ret(r.id)}><RotateCcw/> Return</button>}</div></div>})}</div>{!borrows.length&&<Empty title="No borrowing history" text="Your borrowed resources will appear here."/>}</section>}
function Fines({fines}){return <section><div className="pageIntro"><div><div className="eyebrow">ACCOUNT</div><h1>Fine history</h1><p>Transparent late-return calculations linked to your loans.</p></div></div><div className="fineSummary"><div><span>Total outstanding</span><strong>₹{fines.reduce((a,f)=>a+Number(f.amount),0).toFixed(2)}</strong></div><Receipt/></div><div className="tableWrap"><table><thead><tr><th>Borrow ID</th><th>Due date</th><th>Returned</th><th>Overdue</th><th>Amount</th></tr></thead><tbody>{fines.map(f=><tr key={f.id}><td>#{f.borrowId}</td><td>{f.dueDate}</td><td>{f.returnDate}</td><td>{f.overdueDays} days</td><td className="money">₹{Number(f.amount).toFixed(2)}</td></tr>)}</tbody></table></div>{!fines.length&&<Empty title="No fines" text="Nice work — you have no recorded late-return fines."/>}</section>}
function Admin({books,createBook,updateBook,removeBook,adminBorrows,adminFines,adminUsers,changeRole,ret,refreshAdmin}){const [open,setOpen]=useState(false);const [editingId,setEditingId]=useState(null);const [form,setForm]=useState({title:'',author:'',category:'Computer Science',description:'',ebookUrl:'',totalCopies:5,availableCopies:5});const roleLabel=r=>r==='ADMIN'?'👩‍💼 Librarian/Admin':r==='FACULTY'?'👨‍🏫 Faculty':'🎓 Student';const resetForm=()=>{setEditingId(null);setForm({title:'',author:'',category:'Computer Science',description:'',ebookUrl:'',totalCopies:5,availableCopies:5});setOpen(false)};const startEdit=b=>{setEditingId(b.id);setForm({title:b.title||'',author:b.author||'',category:b.category||'Computer Science',description:b.description||'',ebookUrl:b.ebookUrl||'',totalCopies:b.totalCopies,availableCopies:b.availableCopies});setOpen(true)};return <section><div className="pageIntro"><div><div className="eyebrow">LIBRARIAN / ADMIN CONTROL</div><h1>Library operations</h1><p>Manage resources, users, loans, returns, fines and availability from one workspace.</p></div><div className="adminHeaderActions"><button className="secondary" type="button" onClick={refreshAdmin}><RefreshCw/> Refresh</button><button className="primary" onClick={()=>{if(open)resetForm();else{setEditingId(null);setOpen(true)}}}><Plus/> Add resource</button></div></div>{open&&<form className="adminForm" onSubmit={async e=>{e.preventDefault();if(editingId)await updateBook(editingId,form);else await createBook(form);resetForm()}}><input placeholder="Title" required value={form.title} onChange={e=>setForm({...form,title:e.target.value})}/><input placeholder="Author" required value={form.author} onChange={e=>setForm({...form,author:e.target.value})}/><input placeholder="Category" required value={form.category} onChange={e=>setForm({...form,category:e.target.value})}/><input placeholder="E-book URL (optional)" type="url" value={form.ebookUrl} onChange={e=>setForm({...form,ebookUrl:e.target.value})}/><input type="number" min="1" placeholder="Total copies" value={form.totalCopies} onChange={e=>setForm({...form,totalCopies:e.target.value,availableCopies:editingId?form.availableCopies:e.target.value})}/><input type="number" min="0" max={form.totalCopies} placeholder="Available copies" value={form.availableCopies} onChange={e=>setForm({...form,availableCopies:e.target.value})}/><textarea placeholder="Description" value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/><div className="formActions"><button type="submit" className="primary">{editingId?'Save changes':'Create resource'}</button><button type="button" className="secondary" onClick={resetForm}>Cancel</button></div></form>}<div className="stats"><Stat title="Catalogue" value={books.length} note="Resources" icon={<Library/>}/><Stat title="Active loans" value={adminBorrows.filter(x=>x.status==='ACTIVE').length} note="Across all users" icon={<BookOpen/>}/><Stat title="Recorded fines" value={`₹${adminFines.reduce((a,f)=>a+Number(f.amount),0).toFixed(2)}`} note={`${adminFines.length} records`} icon={<Receipt/>}/></div><div className="adminGrid"><div><div className="sectionHead"><div><h2>Users</h2><p>Students, faculty and library staff</p></div></div><div className="tableWrap"><table><thead><tr><th>User</th><th>Role</th><th>Change role</th></tr></thead><tbody>{adminUsers.map(u=><tr key={u.userId}><td><strong>{u.name}</strong><br/><small>{u.email} · ID #{u.userId}</small></td><td>{roleLabel(u.role)}</td><td><select className="roleSelect" value={u.role==='USER'?'STUDENT':u.role} onChange={e=>changeRole(u.userId,e.target.value)}><option value="STUDENT">Student</option><option value="FACULTY">Faculty</option><option value="ADMIN">Librarian/Admin</option></select></td></tr>)}</tbody></table></div></div><div><div className="sectionHead"><div><h2>Catalogue management</h2><p>Add or remove library resources</p></div></div><div className="tableWrap"><table><thead><tr><th>Resource</th><th>Copies</th><th>Availability</th><th>Action</th></tr></thead><tbody>{books.map(b=><tr key={b.id}><td><strong>{b.title}</strong><br/><small>{b.author}</small></td><td>{b.totalCopies}</td><td>{b.availableCopies}/{b.totalCopies}</td><td><div className="tableActions"><button className="iconBtn" title="Edit resource" onClick={()=>startEdit(b)}><Edit3/></button><button className="iconBtn danger" title="Remove resource" onClick={()=>removeBook(b.id)}><Trash2/></button></div></td></tr>)}</tbody></table></div></div></div><div className="sectionHead adminSection"><div><h2>All circulation</h2><p>Live monitoring · refreshes every 5 seconds · students and faculty</p></div></div><div className="tableWrap"><table><thead><tr><th>Borrow ID</th><th>User</th><th>Book ID</th><th>Status</th><th>Due</th><th>Returned</th><th>Action</th></tr></thead><tbody>{adminBorrows.map(r=>{const u=adminUsers.find(x=>x.userId===r.userId);return <tr key={r.id}><td>#{r.id}</td><td><strong>{u?.name||`User #${r.userId}`}</strong><br/><small>{u?.email||''}</small></td><td>{r.bookId}</td><td><span className={r.status==='ACTIVE'?'pill warn':'pill ok'}>{r.status}</span></td><td>{r.dueDate}</td><td>{r.returnDate||'—'}</td><td>{r.status==='ACTIVE'?<button className="secondary" onClick={()=>ret(r.id)}><RotateCcw/> Mark returned</button>:<span className="muted">Complete</span>}</td></tr>})}</tbody></table></div>{!adminBorrows.length&&<Empty title="No circulation yet" text="Borrowing records will appear here as students and faculty borrow resources."/>}<div className="sectionHead adminSection"><div><h2>All fines</h2><p>Fine records across every borrower</p></div></div><div className="tableWrap"><table><thead><tr><th>Borrow ID</th><th>User</th><th>Due</th><th>Returned</th><th>Overdue</th><th>Amount</th></tr></thead><tbody>{adminFines.map(f=>{const u=adminUsers.find(x=>x.userId===f.userId);return <tr key={f.id}><td>#{f.borrowId}</td><td>{u?.name||`User #${f.userId}`}</td><td>{f.dueDate}</td><td>{f.returnDate}</td><td>{f.overdueDays} days</td><td className="money">₹{Number(f.amount).toFixed(2)}</td></tr>})}</tbody></table></div>{!adminFines.length&&<Empty title="No fines recorded" text="Late-return fines will appear here for library staff review."/>}</section>}
function Empty({title,text}){return <div className="empty"><div className="iconBox"><BookOpen/></div><h3>{title}</h3><p>{text}</p></div>}
class ErrorBoundary extends React.Component{
 constructor(props){super(props);this.state={error:null}}
 static getDerivedStateFromError(error){return {error}}
 render(){
  if(this.state.error)return <div className="authPage"><div className="authCard"><h2>Archvialia needs a reset</h2><p className="muted">The current view hit an unexpected error. Your library data is still stored on the server.</p><button className="primary full" onClick={()=>{localStorage.removeItem('archvialia_token');localStorage.removeItem('archvialia_user');this.setState({error:null});window.location.hash='';window.location.reload()}}>Return to sign in</button></div></div>
  return this.props.children
 }
}
createRoot(document.getElementById('root')).render(<ErrorBoundary><App/></ErrorBoundary>)
