const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();
for(const n of ['der','classes','uc_auth','uc_veic','uc_oit']){const d=JSON.parse(fs.readFileSync(n+'.json'));
 const p=await b.newPage({viewport:{width:d.w,height:d.h},deviceScaleFactor:1.5});
 await p.goto('file://'+process.cwd()+'/'+n+'.html');
 await p.screenshot({path:n+'.png',fullPage:true});
 if(n==='der'||n==='classes') await p.pdf({path:n+'.pdf',width:d.w+'px',height:(d.h+2)+'px',printBackground:true,pageRanges:'1'});}
await b.close();})();
