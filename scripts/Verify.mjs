import fs from 'node:fs';import path from 'node:path';import crypto from 'node:crypto';import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const hash=b=>crypto.createHash('sha256').update(b).digest('hex');
const readJson=p=>JSON.parse(fs.readFileSync(path.join(root,p),'utf8').replace(/^\uFEFF/,''));
function files(dir){return fs.readdirSync(path.join(root,dir),{withFileTypes:true}).flatMap(e=>e.isDirectory()?files(dir+'/'+e.name):[dir+'/'+e.name]).sort();}
const version=readJson('versions.json');
const sourceFiles=['modules','translations'].flatMap(files).sort();
function sourceHash(){return hash(sourceFiles.map(name=>{let content=fs.readFileSync(path.join(root,name));if(!/\.png$/i.test(name))content=Buffer.from(content.toString('utf8').replace(/^\uFEFF/,'').replace(/\r\n/g,'\n'));return name+'\0'+hash(content);}).join('\n'));}
const binaries=['artifacts/plugins','vendor/eclipse-babel'].flatMap(files).filter(f=>f.endsWith('.jar')).sort();
if(process.argv.includes('--snapshot')){
 const snapshot={schema:1,versions:version,sourceDigest:sourceHash(),files:binaries.map(name=>({path:name,sha256:hash(fs.readFileSync(path.join(root,name)))}))};
 fs.writeFileSync(path.join(root,'artifacts/index.json'),JSON.stringify(snapshot,null,2)+'\n');
 console.log('Snapshot de fontes e binários atualizado.');
}
const snapshot=readJson('artifacts/index.json');
if(JSON.stringify(snapshot.versions)!==JSON.stringify(version))throw Error('versions.json mudou; execute o build completo antes de empacotar.');
if(snapshot.sourceDigest!==sourceHash())throw Error('Fontes diferentes dos binários registrados. Execute scripts/Build.ps1 -StudioPath <pasta>.');
if(snapshot.files.length!==binaries.length)throw Error('Quantidade de JARs diverge do índice.');
for(const entry of snapshot.files)if(hash(fs.readFileSync(path.join(root,entry.path)))!==entry.sha256)throw Error('SHA256 divergente: '+entry.path);
function decode(s){return s.replace(/\\u([a-fA-F0-9]{4})|\\(.)/g,(m,h,c)=>h?String.fromCharCode(parseInt(h,16)):({n:'\n',r:'\r',t:'\t',f:'\f'}[c]??c));}
function properties(text){let lines=[],current='';for(let line of text.split(/\r?\n/)){if(current)line=line.trimStart();current+=line;if(/(?<!\\)(?:\\\\)*\\$/.test(current)){current=current.slice(0,-1);continue;}lines.push(current);current='';}return Object.fromEntries(lines.filter(l=>l.trim()&&!/^\s*[#!]/.test(l)).map(l=>{const m=l.match(/^\s*((?:\\.|[^=:\s])+)(?:\s*[:=]\s*|\s+)?(.*)$/);return m?[decode(m[1]),decode(m[2])]:null;}).filter(Boolean));}
function params(s){return [...s.matchAll(/\{\d[^}]*\}|\$[PFRV]\{[^}]*\}|%(?:\d+\$)?[sdif]/g)].map(m=>m[0]).sort().join('|');}
let resources=0,keys=0;
for(const file of files('translations/fragments').filter(f=>f.endsWith('_pt_BR.properties'))){
 const parts=file.split('/');const fragment=parts[2];const host=fragment.replace(/^com\.salutem\.l10n\./,'').replace(/\.pt_BR$/,'');
 const relative=parts.slice(3).join('/').replace(/_pt_BR\.properties$/,'.properties');
 const en=properties(fs.readFileSync(path.join(root,'translations/reference',host,relative),'utf8'));
 const pt=properties(fs.readFileSync(path.join(root,file),'utf8'));
 if(JSON.stringify(Object.keys(en).sort())!==JSON.stringify(Object.keys(pt).sort()))throw Error('Chaves divergentes: '+file);
 for(const key of Object.keys(en)){if(params(en[key])!==params(pt[key]))throw Error('Parâmetros divergentes: '+file+':'+key);if(/ZXPH|ZXID/.test(pt[key]))throw Error('Marcador residual: '+file);}
 resources++;keys+=Object.keys(pt).length;
}
const lang=properties(fs.readFileSync(path.join(root,'modules/language/resources/translations.properties'),'utf8'));
for(const [en,pt] of Object.entries({'Problems':'Problemas','Repository Explorer':'Explorador de Repositórios','Text Field':'Campo de Texto','Page Header':'Cabeçalho da Página','Java Bean':'Java Bean','Data Adapters':'Banco de Dados','Format Factory Class':'Classe da Fábrica de Formatação'}))if(lang[en]!==pt)throw Error('Rótulo regressivo: '+en);
const palettes=properties(fs.readFileSync(path.join(root,'modules/theme/resources/palettes.properties'),'utf8'));
function luminance(hex){const rgb=hex.match(/../g).map(c=>parseInt(c,16)/255).map(c=>c<=0.04045?c/12.92:((c+0.055)/1.055)**2.4);return .2126*rgb[0]+.7152*rgb[1]+.0722*rgb[2];}
function contrast(a,b){const x=luminance(a),y=luminance(b);return (Math.max(x,y)+.05)/(Math.min(x,y)+.05);}
for(const id of palettes.ids.split(',')){
 for(const surface of ['background','input','toolbar','surface','accent'])if(contrast(palettes[id+'.foreground'],palettes[id+'.'+surface])<4.5)throw Error('Contraste insuficiente: '+id+'/'+surface);
 if(contrast(palettes[id+'.muted'],palettes[id+'.background'])<4.5)throw Error('Contraste insuficiente do texto secundário: '+id);
 if(!fs.existsSync(path.join(root,'modules/theme/resources/css',palettes[id+'.css'])))throw Error('CSS ausente: '+id);
}
for(const file of [...sourceFiles,...files('installer'),...files('scripts')].filter(f=>/\.(java|ps1|bat|mjs|properties|xml|ini)$/i.test(f))){
 const text=fs.readFileSync(path.join(root,file),'utf8');
 if(/[A-Z]:\\Users\\[^\\\r\n]+/i.test(text))throw Error('Possível caminho/dado privado: '+file);
}
console.log(`PASS: ${snapshot.files.length} JARs; ${resources} recursos; ${keys} chaves; fontes, hashes e parâmetros preservados.`);
