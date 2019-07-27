/** X3DOM Runtime, http://www.x3dom.org/ 1.5.0-dev - 2e6fa03fee431113695e1bacbe9a0a9552e6cbbf - Fri Apr 12 11:43:52 2013 +0200 */
if(!Array.forEach){Array.forEach=function(array,fun,thisp){var len=array.length;for(var i=0;i<len;i++){if(i in array){fun.call(thisp,array[i],i,array);}}};}
if(!Array.map){Array.map=function(array,fun,thisp){var len=array.length;var res=[];for(var i=0;i<len;i++){if(i in array){res[i]=fun.call(thisp,array[i],i,array);}}
return res;};}
if(!Array.filter){Array.filter=function(array,fun,thisp){var len=array.length;var res=[];for(var i=0;i<len;i++){if(i in array){var val=array[i];if(fun.call(thisp,val,i,array)){res.push(val);}}}
return res;};}
var x3dom={canvases:[]};x3dom.x3dNS='http://www.web3d.org/specifications/x3d-namespace';x3dom.x3dextNS='http://philip.html5.org/x3d/ext';x3dom.xsltNS='http://www.w3.org/1999/XSL/x3dom.Transform';x3dom.xhtmlNS='http://www.w3.org/1999/xhtml';x3dom.nodeTypes={};x3dom.nodeTypesLC={};x3dom.components={};x3dom.geoCache=[];x3dom.caps={PLATFORM:navigator.platform,AGENT:navigator.userAgent};x3dom.registerNodeType=function(nodeTypeName,componentName,nodeDef){if(x3dom.components[componentName]===undefined){x3dom.components[componentName]={};}
nodeDef._typeName=nodeTypeName;nodeDef._compName=componentName;x3dom.components[componentName][nodeTypeName]=nodeDef;x3dom.nodeTypes[nodeTypeName]=nodeDef;x3dom.nodeTypesLC[nodeTypeName.toLowerCase()]=nodeDef;};x3dom.isX3DElement=function(node){return(node.nodeType===Node.ELEMENT_NODE&&node.localName&&(x3dom.nodeTypes[node.localName]||x3dom.nodeTypesLC[node.localName.toLowerCase()]||node.localName.toLowerCase()==="x3d"||node.localName.toLowerCase()==="websg"||node.localName.toLowerCase()==="scene"||node.localName.toLowerCase()==="route"));};x3dom.extend=function(f){function g(){}
g.prototype=f.prototype||f;return new g();};x3dom.getStyle=function(oElm,strCssRule){var strValue;if(window&&window.getComputedStyle&&window.getComputedStyle(oElm,"")){strValue=window.getComputedStyle(oElm,"")[strCssRule];}
else if(oElm.currentStyle){strCssRule=strCssRule.replace(/\-(\w)/g,function(strMatch,p1){return p1.toUpperCase();});strValue=oElm.currentStyle[strCssRule];}
return strValue;};function defineClass(parent,ctor,methods){function inheritance(){}
if(parent){inheritance.prototype=parent.prototype;ctor.prototype=new inheritance();ctor.prototype.constructor=ctor;ctor.superClass=parent;}
if(methods){for(var m in methods){ctor.prototype[m]=methods[m];}}
return ctor;}
x3dom.isa=function(object,clazz){if(!object){return false;}
if(object.constructor===clazz){return true;}
if(object.constructor.superClass===undefined){return false;}
function f(c){if(c===clazz){return true;}
if(c.prototype&&c.prototype.constructor&&c.prototype.constructor.superClass){return f(c.prototype.constructor.superClass);}
return false;}
return f(object.constructor.superClass);};x3dom.getGlobal=function(){return(function(){return this;}).call(null);};x3dom.loadJS=function(src,path_prefix,blocking){var blocking=(blocking===false)?blocking:true;if(blocking){var req;var url=(path_prefix)?path_prefix.trim()+src:src;if(window.XMLHttpRequest){req=new XMLHttpRequest();}else{req=new ActiveXObject("Microsoft.XMLHTTP");}
if(req){req.open("GET",url,false);req.send(null);eval(req.responseText);}}else{var head=document.getElementsByTagName('HEAD').item(0);var script=document.createElement("script");var loadpath=(path_prefix)?path_prefix.trim()+src:src;if(head){x3dom.debug.logError("Trying to load external JS file: "+loadpath);script.type="text/javascript";script.src=loadpath;head.appendChild(script,head.firstChild);}else{alert("No document object found. Can't load components");}}};function array_to_object(a){var o={};for(var i=0;i<a.length;i++){o[a[i]]='';}
return o;}
window.requestAnimFrame=(function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||window.oRequestAnimationFrame||window.msRequestAnimationFrame||function(callback,element){window.setTimeout(callback,16);};})();x3dom.debug={INFO:"INFO",WARNING:"WARNING",ERROR:"ERROR",EXCEPTION:"EXCEPTION",isActive:false,isFirebugAvailable:false,isSetup:false,isAppend:false,numLinesLogged:0,maxLinesToLog:10000,logContainer:null,setup:function(){if(x3dom.debug.isSetup){return;}
try{if(window.console.firebug!==undefined){x3dom.debug.isFirebugAvailable=true;}}
catch(err){x3dom.debug.isFirebugAvailable=false;}
x3dom.debug.setupLogContainer();x3dom.debug.isSetup=true;},activate:function(visible){x3dom.debug.isActive=true;x3dom.debug.logContainer.style.display=(visible)?"block":"none";if(!x3dom.debug.isAppend){if(navigator.appName=="Microsoft Internet Explorer"){x3dom.debug.logContainer.style.marginLeft="8px";document.documentElement.appendChild(x3dom.debug.logContainer);}else{document.body.appendChild(x3dom.debug.logContainer);}
x3dom.debug.isAppend=true;}},setupLogContainer:function(){x3dom.debug.logContainer=document.createElement("div");x3dom.debug.logContainer.id="x3dom_logdiv";x3dom.debug.logContainer.setAttribute("class","x3dom-logContainer");x3dom.debug.logContainer.style.clear="both";},doLog:function(msg,logType){if(!x3dom.debug.isActive){return;}
if(x3dom.debug.numLinesLogged===x3dom.debug.maxLinesToLog){msg="Maximum number of log lines (="+x3dom.debug.maxLinesToLog+") reached. Deactivating logging...";}
if(x3dom.debug.numLinesLogged>x3dom.debug.maxLinesToLog){return;}
var node=document.createElement("p");node.style.margin=0;switch(logType){case x3dom.debug.INFO:node.style.color="#00ff00";break;case x3dom.debug.WARNING:node.style.color="#cd853f";break;case x3dom.debug.ERROR:node.style.color="#ff4500";break;case x3dom.debug.EXCEPTION:node.style.color="#ffff00";break;default:node.style.color="#00ff00";break;}
try{node.innerHTML=logType+": "+msg;x3dom.debug.logContainer.insertBefore(node,x3dom.debug.logContainer.firstChild);}catch(err){if(window.console.firebug!==undefined){window.console.warn(msg);}}
if(x3dom.debug.isFirebugAvailable){switch(logType){case x3dom.debug.INFO:window.console.info(msg);break;case x3dom.debug.WARNING:window.console.warn(msg);break;case x3dom.debug.ERROR:window.console.error(msg);break;case x3dom.debug.EXCEPTION:window.console.debug(msg);break;default:break;}}
x3dom.debug.numLinesLogged++;},logInfo:function(msg){x3dom.debug.doLog(msg,x3dom.debug.INFO);},logWarning:function(msg){x3dom.debug.doLog(msg,x3dom.debug.WARNING);},logError:function(msg){x3dom.debug.doLog(msg,x3dom.debug.ERROR);},logException:function(msg){x3dom.debug.doLog(msg,x3dom.debug.EXCEPTION);},assert:function(c,msg){if(!c){x3dom.debug.doLog("Assertion failed in "+
x3dom.debug.assert.caller.name+': '+
msg,x3dom.debug.ERROR);}},typeOf:function(obj){var type=typeof obj;return type==="object"&&!obj?"null":type;},exists:function(obj,name,type){type=type||"function";return(obj?this.typeOf(obj[name]):"null")===type;},dumpFields:function(node){var str="";for(var fName in node){str+=(fName+", ");}
str+='\n';x3dom.debug.logInfo(str);return str;}};x3dom.debug.setup();var Request=function(url,onloadCallback,priority){this.url=url;this.priority=priority;this.xhr=new XMLHttpRequest();this.onloadCallbacks=[onloadCallback];var self=this;this.xhr.onload=function(){if(x3dom.DownloadManager.debugOutput){x3dom.debug.logInfo('Download manager received data for URL \''+self.url+'\'.');}
--x3dom.DownloadManager.activeDownloads;if((x3dom.DownloadManager.stallToKeepOrder===false)||(x3dom.DownloadManager.resultGetsStalled(self.priority)===false)){var i;for(i=0;i<self.onloadCallbacks.length;++i){self.onloadCallbacks[i](self.xhr.response);}
x3dom.DownloadManager.removeDownload(self);x3dom.DownloadManager.updateStalledResults();}
else if(x3dom.DownloadManager.debugOutput){x3dom.debug.logInfo('Download manager stalled downloaded result for URL \''+self.url+'\'.');}
x3dom.DownloadManager.tryNextDownload();};};Request.prototype.send=function(){this.xhr.open('GET',encodeURI(this.url),true);this.xhr.responseType='arraybuffer';this.xhr.send(null);if(x3dom.DownloadManager.debugOutput){x3dom.debug.logInfo('Download manager posted XHR for URL \''+this.url+'\'.');}};x3dom.DownloadManager={requests:[],maxDownloads:6,activeDownloads:0,debugOutput:false,stallToKeepOrder:false,toggleDebugOutput:function(flag){this.debugOutput=flag;},toggleStrictReturnOrder:function(flag){this.stallToKeepOrder=false;},removeDownload:function(req){var i,j;var done=false;for(i=0;i<this.requests.length&&!done;++i){if(this.requests[i]){for(j=0;j<this.requests[i].length;++j){if(this.requests[i][j]===req){this.requests[i].splice(j,1);done=true;break;}}}}},tryNextDownload:function(){var firstRequest;var i,j;if(this.activeDownloads<this.maxDownloads){for(i=0;i<this.requests.length&&!firstRequest;++i){if(this.requests[i]){for(j=0;j<this.requests[i].length;++j){if(this.requests[i][j].xhr.readyState===XMLHttpRequest.UNSENT){firstRequest=this.requests[i][j];break;}}}}
if(firstRequest){firstRequest.send();++this.activeDownloads;}}},resultGetsStalled:function(priority){var i;for(i=0;i<priority;++i){if(this.requests[i]&&this.requests[i].length){return true;}}
return false;},updateStalledResults:function(){if(x3dom.DownloadManager.stallToKeepOrder){var i,j,k;var req,pendingRequestFound=false;for(i=0;i<this.requests.length&&!pendingRequestFound;++i){if(this.requests[i]){for(j=0;j<this.requests[i].length;++j){req=this.requests[i][j];if(req.xhr.readyState===XMLHttpRequest.DONE){if(x3dom.DownloadManager.debugOutput){x3dom.debug.logInfo('Download manager releases stalled result for URL \''+req.url+'\'.');}
for(k=0;k<req.onloadCallbacks.length;++k){req.onloadCallbacks[k](req.xhr.response);}
this.requests[i].splice(j,1);}
else{pendingRequestFound=true;}}}}}},get:function(urls,onloadCallbacks,priorities){var i,j,k,r;var found=false;var url,onloadCallback,priority;if(urls.length!==onloadCallbacks.length||urls.length!==priorities.length)
{x3dom.debug.logError('DownloadManager: The number of given urls, onload callbacks and priorities is not equal. Ignoring requests.');return;}
for(k=0;k<urls.length;++k){if(!onloadCallbacks[k]===undefined||!priorities[k]===undefined){x3dom.debug.logError('DownloadManager: No onload callback and / or priority specified. Ignoring request for \"'+url+'\"');continue;}
else{url=urls[k];onloadCallback=onloadCallbacks[k];priority=priorities[k];for(i=0;i<this.requests.length&&!found;++i){if(this.requests[i]){for(j=0;j<this.requests[i].length;++j){if(this.requests[i][j].url===url){this.requests[i][j].onloadCallbacks.push(onloadCallback);if(x3dom.DownloadManager.debugOutput){x3dom.debug.logInfo('Download manager appended onload callback for URL \''+url+'\' to a registered request using the same URL.');}
found=true;break;}}}}
if(!found){r=new Request(url,onloadCallback,priority);if(this.requests[priority]){this.requests[priority].push(r);}
else{this.requests[priority]=[r];}}}}
for(i=0;i<urls.length&&this.activeDownloads<this.maxDownloads;++i){this.tryNextDownload();}}};var JOB_WAITING_FOR_DATA=0;var JOB_DATA_AVAILABLE=1;var JOB_GETTING_PROCESSED=2;var JOB_FINISHED=3;x3dom.RefinementJobManager=function(){var self=this;if(typeof Worker!=='undefined'){this.worker=new Worker(new x3dom.RefinementJobWorker().toBlob());this.worker.postMessage=this.worker.webkitPostMessage||this.worker.postMessage;this.worker.addEventListener('message',function(event){return self.messageFromWorker(event);},false);}
else if(!x3dom.RefinementJobManager.suppressOnWorkersNotSupported){x3dom.RefinementJobManager.suppressOnWorkersNotSupported=true;x3dom.RefinementJobManager.onWorkersNotSupported();}
this.attributes=[];};x3dom.RefinementJobManager.suppressOnTransferablesNotSupported=true;x3dom.RefinementJobManager.suppressOnWorkersNotSupported=false;x3dom.RefinementJobManager.onTransferablesNotSupported=function(){alert('Your browser does not support transferables.\n'+'This application might run slower than expected due to data cloning operations.');};x3dom.RefinementJobManager.onWorkersNotSupported=function(){alert('WebWorkers are not supported by your browser. Unable to use RefinementJobManager.');};x3dom.RefinementJobManager.prototype.addResultBuffer=function(attributeId,bufferView){this.attributes[attributeId]={resultBuffer:bufferView.buffer,resultBufferBytesPerElement:bufferView.BYTES_PER_ELEMENT,jobs:[]};};x3dom.RefinementJobManager.prototype.addRefinementJob=function(attributeId,priority,url,level,finishedCallback,stride,numComponentsList,bitsPerLevelList,readOffsetList,writeOffsetList){var self=this;var job={priority:priority,url:url,level:level,finishedCallback:finishedCallback,stride:stride,numComponentsList:numComponentsList,bitsPerLevelList:bitsPerLevelList,readOffsetList:readOffsetList,writeOffsetList:writeOffsetList,state:JOB_WAITING_FOR_DATA,dataBuffer:{}};this.attributes[attributeId].jobs.push(job);var downloadCallback;(function(attId,url){downloadCallback=function(arrayBuffer){self.jobInputDataLoaded(attId,url,arrayBuffer);};})(attributeId,url);x3dom.DownloadManager.get([url],[downloadCallback],[priority]);};x3dom.RefinementJobManager.prototype.jobInputDataLoaded=function(attributeId,url,dataBuffer){var i;var jobs=this.attributes[attributeId].jobs;for(i=0;i<jobs.length;++i){if(jobs[i].url===url){jobs[i].state=JOB_DATA_AVAILABLE;jobs[i].dataBuffer=dataBuffer;this.tryNextJob(attributeId);}}}
x3dom.RefinementJobManager.prototype.tryNextJob=function(attributeId){var i,job;var jobs=this.attributes[attributeId].jobs;var owningBuffer=true;var availableIndex=-1;var bufferView;for(i=0;i<jobs.length;++i){if(jobs[i].state===JOB_GETTING_PROCESSED){owningBuffer=false;break;}
if(availableIndex===-1&&jobs[i].state===JOB_DATA_AVAILABLE){availableIndex=i;}}
if(owningBuffer&&availableIndex!==-1){job=jobs[availableIndex];job.state=JOB_GETTING_PROCESSED;this.worker.postMessage({msg:'processJob',attributeId:attributeId,level:job.level,stride:job.stride,numComponentsList:job.numComponentsList,bitsPerLevelList:job.bitsPerLevelList,readOffsetList:job.readOffsetList,writeOffsetList:job.writeOffsetList,resultBufferBytesPerElement:this.attributes[attributeId].resultBufferBytesPerElement,dataBuffer:job.dataBuffer,resultBuffer:this.attributes[attributeId].resultBuffer},[job.dataBuffer,this.attributes[attributeId].resultBuffer]);if((job.dataBuffer.byteLength>0||this.attributes[attributeId].resultBuffer.byteLength>0)&&!x3dom.RefinementJobManager.suppressOnTransferablesNotSupported){x3dom.RefinementJobManager.suppressOnTransferablesNotSupported=true;x3dom.RefinementJobManager.onTransferablesNotSupported();}}};x3dom.RefinementJobManager.prototype.processedDataAvailable=function(attributeId,resultBuffer){var i;var jobs=this.attributes[attributeId].jobs;this.attributes[attributeId].resultBuffer=resultBuffer;for(i=0;i<jobs.length;++i){if(jobs[i].state===JOB_GETTING_PROCESSED){jobs[i].state=JOB_FINISHED;jobs[i].finishedCallback(attributeId,this.getBufferView(attributeId));break;}}};x3dom.RefinementJobManager.prototype.continueProcessing=function(attributeId){this.tryNextJob(attributeId);};x3dom.RefinementJobManager.prototype.messageFromWorker=function(message){if(message.data.msg){switch(message.data.msg){case'jobFinished':this.processedDataAvailable(message.data.attributeId,message.data.resultBuffer);break;case'log':x3dom.debug.logInfo('Message from Worker Context: '+message.data.txt);break;}}};x3dom.RefinementJobManager.prototype.getBufferView=function(attributeId){var att=this.attributes[attributeId];switch(att.resultBufferBytesPerElement){case 1:return new Uint8Array(att.resultBuffer);case 2:return new Uint16Array(att.resultBuffer);case 4:return new Uint32Array(att.resultBuffer);default:x3dom.debug.logError('Unable to create BufferView: the given number of '+att.resultBufferBytesPerElement+' bytes per element does not match any Uint buffer type.');}};URL=(typeof URL!=='undefined')?URL:(typeof webkitURL!=='undefined')?webkitURL:undefined;x3dom.RefinementJobWorker=function(){};x3dom.RefinementJobWorker.prototype.subtract=function(v0,v1){return[v0[0]-v1[0],v0[1]-v1[1],v0[2]-v1[2]];};x3dom.RefinementJobWorker.prototype.normalize=function(v){var l=Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);l=1.0/l;return[v[0]*l,v[1]*l,v[2]*l];};x3dom.RefinementJobWorker.prototype.cross=function(v0,v1){return[v0[1]*v1[2]-v0[2]*v1[1],v0[2]*v1[0]-v0[0]*v1[2],v0[0]*v1[1]-v0[1]*v1[0]];};x3dom.RefinementJobWorker.prototype.log=function(logMessage){postMessage({msg:'log',txt:logMessage});};x3dom.RefinementJobWorker.prototype.processJob=function(attributeId,level,stride,numComponentsList,bitsPerLevelList,readOffsetList,writeOffsetList,dataBufferView,resultBufferView)
{var aPrecOff,bPrecOff;if(numComponentsList.length===2&&numComponentsList[0]===3&&numComponentsList[1]===2&&bitsPerLevelList[0]===6&&bitsPerLevelList[1]===2){aPrecOff=resultBufferView.BYTES_PER_ELEMENT*8-2-(level*2);bPrecOff=resultBufferView.BYTES_PER_ELEMENT*8-1-(level*1);addBits_3x2_2x1(dataBufferView,resultBufferView,aPrecOff,bPrecOff);}
else if(numComponentsList.length===2&&numComponentsList[0]===3&&numComponentsList[1]===3&&bitsPerLevelList[0]===6&&bitsPerLevelList[1]===0){aPrecOff=resultBufferView.BYTES_PER_ELEMENT*8-2-(level*2);addBits_3x2_3x2_computeNormals(dataBufferView,resultBufferView,aPrecOff);}
else{addBits(level,stride,numComponentsList,bitsPerLevelList,readOffsetList,writeOffsetList,dataBufferView,resultBufferView);}
postMessage({msg:'jobFinished',attributeId:attributeId,resultBuffer:resultBufferView.buffer},[resultBufferView.buffer]);};x3dom.RefinementJobWorker.prototype.onmessage=function(message){var i,dataBufferBytesPerElement;if(message.data.msg){switch(message.data.msg){case'processJob':dataBufferBytesPerElement=0;for(i=0;i<message.data.bitsPerLevelList.length;++i){dataBufferBytesPerElement+=message.data.bitsPerLevelList[i];}
dataBufferBytesPerElement=Math.ceil(dataBufferBytesPerElement/8.0);processJob(message.data.attributeId,message.data.level,message.data.stride,message.data.numComponentsList,message.data.bitsPerLevelList,message.data.readOffsetList,message.data.writeOffsetList,getBufferView(dataBufferBytesPerElement,message.data.dataBuffer),getBufferView(message.data.resultBufferBytesPerElement,message.data.resultBuffer));break;}}};x3dom.RefinementJobWorker.prototype.getBufferView=function(bytesPerElement,buffer){switch(bytesPerElement){case 1:return new Uint8Array(buffer);case 2:return new Uint16Array(buffer);case 4:return new Uint32Array(buffer);default:log('ERROR: The estimated element length of '+bytesPerElement+' bytes does not match any known Uint buffer type.');break;}};x3dom.RefinementJobWorker.prototype.addBits_3x2_2x1=function(dataBufferView,resultBufferView,aPrecOff,bPrecOff){var idx=0;var n=dataBufferView.length;var i,dataChunk,a1,a2,a3,b1,b2;for(i=0;i<n;++i){dataChunk=dataBufferView[i];a1=(dataChunk&0xC0)>>>6;a1<<=aPrecOff;a2=(dataChunk&0x30)>>>4;a2<<=aPrecOff;a3=(dataChunk&0x0C)>>>2;a3<<=aPrecOff;resultBufferView[idx++]|=a1;resultBufferView[idx++]|=a2
resultBufferView[idx++]|=a3;++idx;b1=(dataChunk&0x02)>>>1;b1<<=bPrecOff;b2=(dataChunk&0x01);b2<<=bPrecOff;resultBufferView[idx++]|=b1;resultBufferView[idx++]|=b2;}};x3dom.RefinementJobWorker.prototype.addBits_3x2_3x2_computeNormals=function(dataBufferView,resultBufferView,aPrecOff){var idx=0;var n=dataBufferView.length;var i,dataChunk,a1,a2,a3,b1,b2,b3,points=0,p=[],e1,e2,nor;for(i=0;i<n;++i){dataChunk=dataBufferView[i];a1=(dataChunk&0xC0)>>>6;a1<<=aPrecOff;a2=(dataChunk&0x30)>>>4;a2<<=aPrecOff;a3=(dataChunk&0x0C)>>>2;a3<<=aPrecOff;resultBufferView[idx++]|=a1;resultBufferView[idx++]|=a2
resultBufferView[idx++]|=a3;p[points]=[resultBufferView[idx-3],resultBufferView[idx-2],resultBufferView[idx-1]];++idx;if(++points===3){points=0;e1=this.normalize(this.subtract(p[1],p[0]));e2=this.normalize(this.subtract(p[2],p[0]));nor=this.normalize(this.cross(e1,e2));b1=nor[0]*32767+32767;b2=nor[1]*32767+32767;b3=nor[2]*32767+32767;resultBufferView[idx]=b1;resultBufferView[idx+1]=b2;resultBufferView[idx+2]=b3;resultBufferView[idx-8]=b1;resultBufferView[idx+1-8]=b2;resultBufferView[idx+2-8]=b3;resultBufferView[idx-8*2]=b1;resultBufferView[idx+1-8*2]=b2;resultBufferView[idx+2-8*2]=b3;}
idx+=4;}};x3dom.RefinementJobWorker.prototype.addBits=function(level,stride,numComponentsList,bitsPerLevelList,readOffsetList,writeOffsetList,dataBufferView,resultBufferView){var i,j,c,nc,attributeLeftShift;var dataChunk;var componentMasksList=[],componentMasks;var componentShiftsList=[],componentShifts;var precisionOffsetList=[],precisionOffset;var m=numComponentsList.length;var strideInElements=stride/(resultBufferView.BYTES_PER_ELEMENT*8);var bitsPerComponentPerLevel;for(i=0;i<m;++i){nc=numComponentsList[i];bitsPerComponentPerLevel=(bitsPerLevelList[i]/numComponentsList[i]);attributeLeftShift=(dataBufferView.BYTES_PER_ELEMENT*8)-readOffsetList[i]-bitsPerComponentPerLevel*nc;precisionOffsetList[i]=(resultBufferView.BYTES_PER_ELEMENT*8)-bitsPerComponentPerLevel-(level*bitsPerComponentPerLevel);componentMasks=[];componentShifts=[];for(c=0;c<nc;++c){componentShifts[c]=attributeLeftShift+(nc-c-1)*bitsPerComponentPerLevel;componentMasks[c]=0|(Math.pow(2,bitsPerComponentPerLevel)-1);componentMasks[c]<<=componentShifts[c];}
componentMasksList.push(componentMasks);componentShiftsList.push(componentShifts);}
var n=dataBufferView.length;var baseIdx,idx;var component;for(j=0;j<m;++j){nc=numComponentsList[j];baseIdx=writeOffsetList[j]/(resultBufferView.BYTES_PER_ELEMENT*8);componentMasks=componentMasksList[j];componentShifts=componentShiftsList[j];precisionOffset=precisionOffsetList[j];for(i=0;i<n;++i){dataChunk=dataBufferView[i];for(c=0;c<nc;++c){component=dataChunk&componentMasks[c];component>>>=componentShifts[c];component<<=precisionOffset;idx=baseIdx+c;resultBufferView[idx]|=component;}
baseIdx+=strideInElements;}}}
x3dom.RefinementJobWorker.prototype.toBlob=function(){var str='';str+='postMessage = (typeof webkitPostMessage !== "undefined") ? webkitPostMessage : postMessage;\n';for(var p in this){if(this[p]!=x3dom.RefinementJobWorker.prototype.toBlob){str+=p+' = ';if(this[p]instanceof String){str+='"'+this[p]+'"';}
else if(this[p]instanceof Array){str+="[];\n";}
else{str+=this[p]+';\n';}}}
var blob=new Blob([str]);return URL.createObjectURL(blob);};x3dom.Properties=function(){this.properties={};};x3dom.Properties.prototype.setProperty=function(name,value){x3dom.debug.logInfo("Properties: Setting property '"+name+"' to value '"+value+"'");this.properties[name]=value;};x3dom.Properties.prototype.getProperty=function(name,def){if(this.properties[name]){return this.properties[name]}else{return def;}};x3dom.Properties.prototype.merge=function(other){for(var attrname in other.properties){this.properties[attrname]=other.properties[attrname];}};x3dom.Properties.prototype.toString=function(){var str="";for(var name in this.properties){str+="Name: "+name+" Value: "+this.properties[name]+"\n";}
return str;};x3dom.DoublyLinkedList=function(){this.length=0;this.first=null;this.last=null;};x3dom.DoublyLinkedList.ListNode=function(point,point_index,normals,colors,texCoords){this.point=point;this.point_index=point_index;this.normals=normals;this.colors=colors;this.texCoords=texCoords;this.next=null;this.prev=null;};x3dom.DoublyLinkedList.prototype.appendNode=function(node){if(this.first===null){node.prev=node;node.next=node;this.first=node;this.last=node;}else{node.prev=this.last;node.next=this.first;this.first.prev=node;this.last.next=node;this.last=node;}
this.length++;};x3dom.DoublyLinkedList.prototype.insertAfterNode=function(node,newNode){newNode.prev=node;newNode.next=node.next;node.next.prev=newNode;node.next=newNode;if(newNode.prev==this.last){this.last=newNode;}
this.length++;};x3dom.DoublyLinkedList.prototype.deleteNode=function(node){if(this.length>1){node.prev.next=node.next;node.next.prev=node.prev;if(node==this.first){this.first=node.next;}
if(node==this.last){this.last=node.prev;}}else{this.first=null;this.last=null;}
node.prev=null;node.next=null;this.length--;};x3dom.DoublyLinkedList.prototype.getNode=function(index){var node=null;if(index>this.length){return node;}
for(var i=0;i<this.length;i++){if(i==0){node=this.first;}else{node=node.next;}
if(i==index){return node;}}
return null;};x3dom.DoublyLinkedList.prototype.invert=function(){var node=null;var tmp=null;node=this.first;for(var i=0;i<this.length;i++){tmp=node.prev;node.prev=node.next;node.next=tmp;node=node.prev;}
tmp=this.first;this.first=this.last;this.last=tmp;};x3dom.EarClipping={reversePointDirection:function(linklist,plane){var l,k;var count=0;var z;var nodei,nodel,nodek;if(linklist.length<3){return false;}
for(var i=0;i<linklist.length;i++){l=(i+1)%linklist.length;k=(i+2)%linklist.length;nodei=linklist.getNode(i);nodel=linklist.getNode(l);nodek=linklist.getNode(k);if(plane=='YZ'){z=(nodel.point.y-nodei.point.y)*(nodek.point.z-nodel.point.z);z-=(nodel.point.z-nodei.point.z)*(nodek.point.y-nodel.point.y);}else if(plane=='XZ'){z=(nodel.point.z-nodei.point.z)*(nodek.point.x-nodel.point.x);z-=(nodel.point.x-nodei.point.x)*(nodek.point.z-nodel.point.z);}else{z=(nodel.point.x-nodei.point.x)*(nodek.point.y-nodel.point.y);z-=(nodel.point.y-nodei.point.y)*(nodek.point.x-nodel.point.x);}
if(z<0){count--;}else if(z>0){count++;}}
if(count<0){linklist.invert();return true;}
return false;},getIndexes:function(linklist){var node=linklist.first.next;var plane=this.identifyPlane(node.prev.point,node.point,node.next.point);var invers=this.reversePointDirection(linklist,plane);var indexes=[];node=linklist.first.next;var next=null;var count=0;var isEar=true;while(linklist.length>=3&&count<15){next=node.next;for(var i=0;i<linklist.length;i++){if(this.isNotEar(linklist.getNode(i).point,node.prev.point,node.point,node.next.point,plane)){isEar=false;}}
if(isEar){if(this.isKonvex(node.prev.point,node.point,node.next.point,plane)){indexes.push(node.prev.point_index,node.point_index,node.next.point_index);linklist.deleteNode(node);}else{count++;}}
node=next;isEar=true;}
if(invers){return indexes.reverse();}else{return indexes;}},getMultiIndexes:function(linklist){var node=linklist.first.next;var plane=this.identifyPlane(node.prev.point,node.point,node.next.point);var invers=this.reversePointDirection(linklist,plane);var data=new Object();data.indices=[];data.point=[];data.normals=[];data.colors=[];data.texCoords=[];node=linklist.first.next;var next=null;var count=0;var isEar=true;while(linklist.length>=3&&count<15){next=node.next;for(var i=0;i<linklist.length;i++){if(this.isNotEar(linklist.getNode(i).point,node.prev.point,node.point,node.next.point,plane)){isEar=false;}}
if(isEar){if(this.isKonvex(node.prev.point,node.point,node.next.point,plane)){data.indices.push(node.prev.point_index,node.point_index,node.next.point_index);data.point.push(node.prev.point,node.point,node.next.point);if(node.normals){data.normals.push(node.prev.normals,node.normals,node.next.normals);}
if(node.colors){data.colors.push(node.prev.colors,node.colors,node.next.colors);}
if(node.texCoords){data.texCoords.push(node.prev.texCoords,node.texCoords,node.next.texCoords);}
linklist.deleteNode(node);}else{count++;}}
node=next;isEar=true;}
if(invers){data.indices=data.indices.reverse();data.point=data.point.reverse();data.normals=data.normals.reverse();data.colors=data.colors.reverse();data.texCoords=data.texCoords.reverse();return data;}else{return data;}},isNotEar:function(ap1,tp1,tp2,tp3,plane){var b0,b1,b2,b3;var ap1a,ap1b,tp1a,tp1b,tp2a,tp2b,tp3a,tp3b;if(plane=='YZ'){ap1a=ap1.y,ap1b=ap1.z;tp1a=tp1.y,tp1b=tp1.z;tp2a=tp2.y,tp2b=tp2.z;tp3a=tp3.y,tp3b=tp3.z;}else if(plane=='XZ'){ap1a=ap1.z,ap1b=ap1.x;tp1a=tp1.z,tp1b=tp1.x;tp2a=tp2.z,tp2b=tp2.x;tp3a=tp3.z,tp3b=tp3.x;}else{ap1a=ap1.x,ap1b=ap1.y;tp1a=tp1.x,tp1b=tp1.y;tp2a=tp2.x,tp2b=tp2.y;tp3a=tp3.x,tp3b=tp3.y;}
b0=((tp2a-tp1a)*(tp3b-tp1b)-(tp3a-tp1a)*(tp2b-tp1b));if(b0!=0){b1=(((tp2a-ap1a)*(tp3b-ap1b)-(tp3a-ap1a)*(tp2b-ap1b))/b0);b2=(((tp3a-ap1a)*(tp1b-ap1b)-(tp1a-ap1a)*(tp3b-ap1b))/b0);b3=1-b1-b2;return((b1>0)&&(b2>0)&&(b3>0));}
else{return false;}},isKonvex:function(p,p1,p2,plane){var pa,pb,p1a,p1b,p2a,p2b;if(plane=='YZ'){pa=p.y,pb=p.z;p1a=p1.y,p1b=p1.z;p2a=p2.y,p2b=p2.z;}else if(plane=='XZ'){pa=p.z,pb=p.x;p1a=p1.z,p1b=p1.x;p2a=p2.z,p2b=p2.x;}else{pa=p.x,pb=p.y;p1a=p1.x,p1b=p1.y;p2a=p2.x,p2b=p2.y;}
var l=((p1a-pa)*(p2b-pb)-(p1b-pb)*(p2a-pa));if(l<0){return false;}else{return true;}},identifyPlane:function(p1,p2,p3){var v1x,v1y,v1z;var v2x,v2y,v2z;var v3x,v3y,v3z;v1x=p2.x-p1.x,v1y=p2.y-p1.y,v1z=p2.z-p1.z;v2x=p3.x-p1.x,v2y=p3.y-p1.y,v2z=p3.z-p1.z;v3x=v1y*v2z-v1z*v2y;v3y=v1z*v2x-v1x*v2z;v3z=v1x*v2y-v1y*v2x;var angle=Math.max(Math.abs(v3x),Math.abs(v3y),Math.abs(v3z));if(angle==Math.abs(v3x)){return'YZ';}else if(angle==Math.abs(v3y)){return'XZ';}else if(angle==Math.abs(v3z)){return'XY';}else{return'fehler';}}};x3dom.Utils={};x3dom.Utils.measurements=[];window.performance=window.performance||{};performance.now=(function(){return performance.now||performance.mozNow||performance.msNow||performance.oNow||performance.webkitNow||function(){return new Date().getTime();};})();x3dom.Utils.startMeasure=function(name){var uname=name.toUpperCase();if(!x3dom.Utils.measurements[uname]){if(performance&&performance.now){x3dom.Utils.measurements[uname]=performance.now();}else{x3dom.Utils.measurements[uname]=new Date().getTime();}}};x3dom.Utils.stopMeasure=function(name){var uname=name.toUpperCase();if(x3dom.Utils.measurements[uname]){var startTime=x3dom.Utils.measurements[uname];delete x3dom.Utils.measurements[uname];if(performance&&performance.now){return performance.now()-startTime;}else{return new Date().getTime()-startTime;}}
return 0;};x3dom.Utils.isNumber=function(n){return!isNaN(parseFloat(n))&&isFinite(n);};x3dom.Utils.createTexture2D=function(gl,doc,src,bgnd,withCredentials)
{doc.downloadCount++;var texture=gl.createTexture();var image=new Image();image.crossOrigin=withCredentials?'use-credentials':'';image.src=src;image.onload=function(){image=x3dom.Utils.scaleImage(image);if(bgnd==true){gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL,true);}
gl.bindTexture(gl.TEXTURE_2D,texture);gl.texImage2D(gl.TEXTURE_2D,0,gl.RGBA,gl.RGBA,gl.UNSIGNED_BYTE,image);gl.bindTexture(gl.TEXTURE_2D,null);if(bgnd==true){gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL,false);}
texture.width=image.width;texture.height=image.height;doc.downloadCount--;doc.needRender=true;};image.onerror=function(){x3dom.debug.logError("[Utils|createTexture2D] Can't load Image: "+src);doc.downloadCount--;};return texture;};x3dom.Utils.generateNonIndexedTriangleData=function(indices,positions,normals,texCoords,colors,newPositions,newNormals,newTexCoords,newColors)
{for(i=0;i<indices.length;i+=3){var i0=indices[i],i1=indices[i+1],i2=indices[i+2];if(positions){var p0=new x3dom.fields.SFVec3f(),p1=new x3dom.fields.SFVec3f(),p2=new x3dom.fields.SFVec3f();p0.setValues(positions[i0]);p1.setValues(positions[i1]);p2.setValues(positions[i2]);newPositions.push(p0);newPositions.push(p1);newPositions.push(p2);}
if(normals){var n0=new x3dom.fields.SFVec3f(),n1=new x3dom.fields.SFVec3f(),n2=new x3dom.fields.SFVec3f();n0.setValues(normals[i0]);n1.setValues(normals[i1]);n2.setValues(normals[i2]);newNormals.push(n0);newNormals.push(n1);newNormals.push(n2);}
if(texCoords){var t0=new x3dom.fields.SFVec2f(),t1=new x3dom.fields.SFVec2f(),t2=new x3dom.fields.SFVec2f();t0.setValues(texCoords[i0]);t1.setValues(texCoords[i1]);t1.setValues(texCoords[i2]);newTexCoords.push(t0);newTexCoords.push(t1);newTexCoords.push(t2);}
if(colors){var c0=new x3dom.fields.SFVec3f(),c1=new x3dom.fields.SFVec3f(),c2=new x3dom.fields.SFVec3f();c0.setValues(texCoords[i0]);c1.setValues(texCoords[i1]);c1.setValues(texCoords[i2]);newColors.push(c0);newColors.push(c1);newColors.push(c2);}}};x3dom.Utils.createTextureCube=function(gl,doc,url,bgnd,withCredentials)
{var texture=gl.createTexture();var faces;if(bgnd){faces=[gl.TEXTURE_CUBE_MAP_POSITIVE_Z,gl.TEXTURE_CUBE_MAP_NEGATIVE_Z,gl.TEXTURE_CUBE_MAP_POSITIVE_Y,gl.TEXTURE_CUBE_MAP_NEGATIVE_Y,gl.TEXTURE_CUBE_MAP_POSITIVE_X,gl.TEXTURE_CUBE_MAP_NEGATIVE_X];}
else
{faces=[gl.TEXTURE_CUBE_MAP_NEGATIVE_Z,gl.TEXTURE_CUBE_MAP_POSITIVE_Z,gl.TEXTURE_CUBE_MAP_NEGATIVE_Y,gl.TEXTURE_CUBE_MAP_POSITIVE_Y,gl.TEXTURE_CUBE_MAP_NEGATIVE_X,gl.TEXTURE_CUBE_MAP_POSITIVE_X];}
texture.pendingTextureLoads=-1;texture.textureCubeReady=false;var width=0,height=0;for(var i=0;i<faces.length;i++){var face=faces[i];var image=new Image();image.crossOrigin=withCredentials?'use-credentials':'';texture.pendingTextureLoads++;doc.downloadCount++;image.onload=function(texture,face,image,swap){return function(){if(width==0&&height==0){width=image.width;height=image.height;}
else if(width!=image.width||height!=image.height){x3dom.debug.logWarning("[Utils|createTextureCube] Rescaling CubeMap images, which are of different size!");image=x3dom.Utils.rescaleImage(image,width,height);}
gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL,swap);gl.bindTexture(gl.TEXTURE_CUBE_MAP,texture);gl.texImage2D(face,0,gl.RGBA,gl.RGBA,gl.UNSIGNED_BYTE,image);gl.bindTexture(gl.TEXTURE_CUBE_MAP,null);gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL,false);texture.pendingTextureLoads--;doc.downloadCount--;if(texture.pendingTextureLoads<0){texture.textureCubeReady=true;x3dom.debug.logInfo("[Utils|createTextureCube] Loading CubeMap finished...");doc.needRender=true;}};}(texture,face,image,bgnd);image.onerror=function()
{doc.downloadCount--;x3dom.debug.logError("[Utils|createTextureCube] Can't load CubeMap!");};image.src=url[i];}
return texture;};x3dom.Utils.getFileName=function(url)
{var filename;if(url.lastIndexOf("/")>-1){filename=url.substr(url.lastIndexOf("/")+1);}
else if(url.lastIndexOf("\\")>-1){filename=url.substr(url.lastIndexOf("\\")+1);}
else{filename=url;}
return filename;};x3dom.Utils.findTextureByName=function(texture,name)
{for(var i=0;i<texture.length;++i)
{if(name==texture[i].samplerName)
return texture[i];}
return false;};x3dom.Utils.rescaleImage=function(image,width,height)
{var canvas=document.createElement("canvas");canvas.width=width;canvas.height=height;canvas.getContext("2d").drawImage(image,0,0,image.width,image.height,0,0,canvas.width,canvas.height);return canvas;};x3dom.Utils.scaleImage=function(image)
{if(!x3dom.Utils.isPowerOfTwo(image.width)||!x3dom.Utils.isPowerOfTwo(image.height)){var canvas=document.createElement("canvas");canvas.width=x3dom.Utils.nextHighestPowerOfTwo(image.width);canvas.height=x3dom.Utils.nextHighestPowerOfTwo(image.height);var ctx=canvas.getContext("2d");ctx.drawImage(image,0,0,image.width,image.height,0,0,canvas.width,canvas.height);image=canvas;}
return image;}
x3dom.Utils.isPowerOfTwo=function(x)
{return((x&(x-1))===0);};x3dom.Utils.nextHighestPowerOfTwo=function(x)
{--x;for(var i=1;i<32;i<<=1){x=x|x>>i;}
return(x+1);};x3dom.Utils.nextBestPowerOfTwo=function(x)
{var log2x=Math.log(x)/Math.log(2);return Math.pow(2,Math.round(log2x));};x3dom.Utils.getDataTypeSize=function(type)
{switch(type)
{case"Int8":case"Uint8":return 1;case"Int16":case"Uint16":return 2;case"Int32":case"Uint32":case"Float32":return 4;case"Float64":default:return 8;}};x3dom.Utils.getVertexAttribType=function(type,gl)
{var dataType=gl.NONE;switch(type)
{case"Int8":dataType=gl.BYTE;break;case"Uint8":dataType=gl.UNSIGNED_BYTE;break;case"Int16":dataType=gl.SHORT;break;case"Uint16":dataType=gl.UNSIGNED_SHORT;break;case"Int32":dataType=gl.INT;break;case"Uint32":dataType=gl.UNSIGNED_INT;break;case"Float32":dataType=gl.FLOAT;break;case"Float64":default:x3dom.debug.logError("Can't find this.gl data type for "+type+", getting FLOAT...");dataType=gl.FLOAT;break;}
return dataType;};x3dom.Utils.getArrayBufferView=function(type,buffer)
{var array=null;switch(type)
{case"Int8":array=new Int8Array(buffer);break;case"Uint8":array=new Uint8Array(buffer);break;case"Int16":array=new Int16Array(buffer);break;case"Uint16":array=new Uint16Array(buffer);break;case"Int32":array=new Int32Array(buffer);break;case"Uint32":array=new Uint32Array(buffer);break;case"Float32":array=new Float32Array(buffer);break;case"Float64":array=new Float64Array(buffer);break;default:x3dom.debug.logError("Can't create typed array view of type "+type+", trying Float32...");array=new Float32Array(buffer);break;}
return array;};x3dom.Utils.isUnsignedType=function(str)
{return(str=="Uint8"||str=="Uint16"||str=="Uint16"||str=="Uint32");};x3dom.Utils.checkDirtyLighting=function(viewarea)
{return[viewarea.getLights().length+viewarea._scene.getNavigationInfo()._vf.headlight,viewarea.getLightsShadow()];};x3dom.Utils.minFilterDic=function(gl,minFilter)
{switch(minFilter)
{case"NEAREST":return gl.NEAREST;case"LINEAR":return gl.LINEAR;case"NEAREST_MIPMAP_NEAREST":return gl.NEAREST_MIPMAP_NEAREST;case"NEAREST_MIPMAP_LINEAR":return gl.NEAREST_MIPMAP_LINEAR;case"LINEAR_MIPMAP_NEAREST":return gl.LINEAR_MIPMAP_NEAREST;case"LINEAR_MIPMAP_LINEAR":return gl.LINEAR_MIPMAP_LINEAR;case"AVG_PIXEL":return gl.LINEAR;case"AVG_PIXEL_AVG_MIPMAP":return gl.LINEAR_MIPMAP_LINEAR;case"AVG_PIXEL_NEAREST_MIPMAP":return gl.LINEAR_MIPMAP_NEAREST;case"DEFAULT":return gl.LINEAR_MIPMAP_LINEAR;case"FASTEST":return gl.NEAREST;case"NEAREST_PIXEL":return gl.NEAREST;case"NEAREST_PIXEL_AVG_MIPMAP":return gl.NEAREST_MIPMAP_LINEAR;case"NEAREST_PIXEL_NEAREST_MIPMAP":return gl.NEAREST_MIPMAP_NEAREST;case"NICEST":return gl.LINEAR_MIPMAP_LINEAR;default:return gl.LINEAR;}};x3dom.Utils.magFilterDic=function(gl,magFilter)
{switch(magFilter)
{case"NEAREST":return gl.NEAREST;case"LINEAR":return gl.LINEAR;case"AVG_PIXEL":return gl.LINEAR;case"DEFAULT":return gl.LINEAR;case"FASTEST":return gl.NEAREST;case"NEAREST_PIXEL":return gl.NEAREST;case"NICEST":return gl.LINEAR;default:return gl.LINEAR;}};x3dom.Utils.boundaryModesDic=function(gl,mode)
{switch(mode)
{case"CLAMP":return gl.CLAMP_TO_EDGE;case"CLAMP_TO_EDGE":return gl.CLAMP_TO_EDGE;case"CLAMP_TO_BOUNDARY":return gl.CLAMP_TO_EDGE;case"MIRRORED_REPEAT":return gl.MIRRORED_REPEAT;case"REPEAT":return gl.REPEAT;default:return gl.REPEAT;}};x3dom.Utils.generateProperties=function(viewarea,shape)
{var property={};var geometry=shape._cf.geometry.node;var appearance=shape._cf.appearance.node;var texture=appearance?appearance._cf.texture.node:null;var material=appearance?shape._cf.appearance.node._cf.material.node:null;if(appearance&&appearance._shader&&x3dom.isa(appearance._shader,x3dom.nodeTypes.ComposedShader)){property.CSHADER=shape._objectID;}
else if(geometry){property.CSHADER=-1;property.SOLID=(shape.isSolid())?1:0;property.TEXT=(x3dom.isa(geometry,x3dom.nodeTypes.Text))?1:0;property.POPGEOMETRY=(x3dom.isa(geometry,x3dom.nodeTypes.PopGeometry))?1:0;property.BITLODGEOMETRY=(x3dom.isa(geometry,x3dom.nodeTypes.BitLODGeometry))?1:0;property.IMAGEGEOMETRY=(x3dom.isa(geometry,x3dom.nodeTypes.ImageGeometry))?1:0;property.IG_PRECISION=(property.IMAGEGEOMETRY)?geometry.numCoordinateTextures():0;property.IG_INDEXED=(property.IMAGEGEOMETRY&&geometry.getIndexTexture()!=null)?1:0;property.POINTLINE2D=x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.PointSet)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.IndexedLineSet)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Polypoint2D)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Polyline2D)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Arc2D)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Circle2D)?1:0;property.SHADOW=(viewarea.getLightsShadow())?1:0;property.FOG=(viewarea._scene.getFog()._vf.visibilityRange>0)?1:0;property.CSSHADER=(appearance&&appearance._shader&&x3dom.isa(appearance._shader,x3dom.nodeTypes.CommonSurfaceShader))?1:0;property.LIGHTS=(!property.POINTLINE2D&&appearance&&(material||property.CSSHADER))?(viewarea.getLights().length)+(viewarea._scene.getNavigationInfo()._vf.headlight):0;property.TEXTURED=(texture||property.TEXT)?1:0;property.TEXTRAFO=(appearance&&appearance._cf.textureTransform.node)?1:0;property.DIFFUSEMAP=(property.CSSHADER&&appearance._shader.getDiffuseMap())?1:0;property.NORMALMAP=(property.CSSHADER&&appearance._shader.getNormalMap())?1:0;property.SPECMAP=(property.CSSHADER&&appearance._shader.getSpecularMap())?1:0;property.CUBEMAP=(texture&&x3dom.isa(texture,x3dom.nodeTypes.X3DEnvironmentTextureNode))?1:0;property.BLENDING=(property.TEXT||property.CUBEMAP||(texture&&texture._blending))?1:0;property.REQUIREBBOX=(geometry._vf.coordType!==undefined&&geometry._vf.coordType!="Float32")?1:0;property.REQUIREBBOXNOR=(geometry._vf.normalType!==undefined&&geometry._vf.normalType!="Float32")?1:0;property.REQUIREBBOXCOL=(geometry._vf.colorType!==undefined&&geometry._vf.colorType!="Float32")?1:0;property.REQUIREBBOXTEX=(geometry._vf.texCoordType!==undefined&&geometry._vf.texCoordType!="Float32")?1:0;property.COLCOMPONENTS=geometry._mesh._numColComponents;property.NORCOMPONENTS=geometry._mesh._numNormComponents;property.POSCOMPONENTS=geometry._mesh._numPosComponents;property.SPHEREMAPPING=(geometry._cf.texCoord!==undefined&&geometry._cf.texCoord.node!==null&&geometry._cf.texCoord.node._vf.mode&&geometry._cf.texCoord.node._vf.mode.toLowerCase()=="sphere")?1:0;property.VERTEXCOLOR=(geometry._mesh._colors[0].length>0||(property.IMAGEGEOMETRY&&geometry.getColorTexture())||(property.BITLODGEOMETRY&&geometry.hasColor())||(property.POPGEOMETRY&&geometry.hasColor())||(geometry._vf.color!==undefined&&geometry._vf.color.length>0))?1:0;}
property.toIdentifier=function(){var id="";for(var p in this){if(this[p]!=this.toIdentifier&&this[p]!=this.toString){id+=this[p];}}
return id;};property.toString=function(){var str="";for(var p in this){if(this[p]!=this.toIdentifier&&this[p]!=this.toString){str+=p+": "+this[p]+", ";}}
return str;};return property;};x3dom.Utils.wrapProgram=function(gl,program)
{var shader={};shader.bind=function(){gl.useProgram(program);};var i=0;var loc=null;var obj=null;var glErr;var numUniforms=gl.getProgramParameter(program,gl.ACTIVE_UNIFORMS);for(i=0;i<numUniforms;++i){try{obj=gl.getActiveUniform(program,i);}
catch(eu){}
glErr=gl.getError();if(glErr!==0){x3dom.debug.logError("GL-Error (on searching uniforms): "+glErr);}
loc=gl.getUniformLocation(program,obj.name);switch(obj.type){case gl.SAMPLER_2D:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform1i(loc,val);};})(loc));break;case gl.SAMPLER_CUBE:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform1i(loc,val);};})(loc));break;case gl.BOOL:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform1i(loc,val);};})(loc));break;case gl.FLOAT:if(obj.name.indexOf("[0]")!=-1)
shader.__defineSetter__(obj.name.substring(0,obj.name.length-3),(function(loc){return function(val){gl.uniform1fv(loc,new Float32Array(val));};})(loc));else
shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform1f(loc,val);};})(loc));break;case gl.FLOAT_VEC2:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform2f(loc,val[0],val[1]);};})(loc));break;case gl.FLOAT_VEC3:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform3f(loc,val[0],val[1],val[2]);};})(loc));break;case gl.FLOAT_VEC4:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform4f(loc,val[0],val[1],val[2],val[3]);};})(loc));break;case gl.FLOAT_MAT2:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniformMatrix2fv(loc,false,new Float32Array(val));};})(loc));break;case gl.FLOAT_MAT3:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniformMatrix3fv(loc,false,new Float32Array(val));};})(loc));break;case gl.FLOAT_MAT4:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniformMatrix4fv(loc,false,new Float32Array(val));};})(loc));break;case gl.INT:shader.__defineSetter__(obj.name,(function(loc){return function(val){gl.uniform1i(loc,val);};})(loc));break;default:x3dom.debug.logWarning('GLSL program variable '+obj.name+' has unknown type '+obj.type);}}
var numAttribs=gl.getProgramParameter(program,gl.ACTIVE_ATTRIBUTES);for(i=0;i<numAttribs;++i){try{obj=gl.getActiveAttrib(program,i);}
catch(ea){}
glErr=gl.getError();if(glErr!==0){x3dom.debug.logError("GL-Error (on searching attributes): "+glErr);}
loc=gl.getAttribLocation(program,obj.name);shader[obj.name]=loc;}
return shader;};x3dom.States=function(x3dElem){var that=this;this.active=false;this.viewer=document.createElement('div');this.viewer.id='x3dom-state-viewer';var title=document.createElement('div');title.className='x3dom-states-head';var subTitle=document.createElement('span');subTitle.className='x3dom-states-head2';title.appendChild(subTitle);this.measureList=document.createElement('ul');this.measureList.className='x3dom-states-list';this.infoList=document.createElement('ul');this.infoList.className='x3dom-states-list';this.viewer.appendChild(title);this.viewer.appendChild(this.measureList);this.viewer.appendChild(this.infoList);this.disableContextMenu=function(e){e.preventDefault();e.stopPropagation();e.returnValue=false;return false;};this.thousandSeperator=function(value){return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g,",");};this.toFixed=function(value){var fixed=(value<1)?2:(value<10)?2:2;return value.toFixed(fixed);};this.update=function(){var infos=x3dElem.runtime.states.infos;var measurements=x3dElem.runtime.states.measurements;this.measureList.innerHTML="";for(var m in measurements){infoItem=document.createElement('li');infoItem.className='x3dom-states-item';infoTitle=document.createElement('div');infoTitle.className='x3dom-states-item-title';infoTitle.appendChild(document.createTextNode(m));infoValue=document.createElement('div');infoValue.className='x3dom-states-item-value';infoValue.appendChild(document.createTextNode(this.toFixed(measurements[m])));infoItem.appendChild(infoTitle);infoItem.appendChild(infoValue);this.measureList.appendChild(infoItem);}
this.infoList.innerHTML="";for(var i in infos){var infoItem=document.createElement('li');infoItem.className='x3dom-states-item';var infoTitle=document.createElement('div');infoTitle.className='x3dom-states-item-title';infoTitle.appendChild(document.createTextNode(i));var infoValue=document.createElement('div');infoValue.className='x3dom-states-item-value';infoValue.appendChild(document.createTextNode(this.thousandSeperator(infos[i])));infoItem.appendChild(infoTitle);infoItem.appendChild(infoValue);this.infoList.appendChild(infoItem);}};window.setInterval(function(){that.update();},1000);this.viewer.addEventListener("contextmenu",that.disableContextMenu);};x3dom.States.prototype.display=function(value){this.active=(value!==undefined)?value:!this.active;this.viewer.style.display=(this.active)?"block":"none";};x3dom.BinaryContainerLoader={};x3dom.BinaryContainerLoader.setupBinGeo=function(shape,sp,gl,viewarea,currContext)
{var t00=new Date().getTime();shape._webgl.binaryGeometry=-1;shape._webgl.internalDownloadCount=((shape._cf.geometry.node._vf.index.length>0)?1:0)+
((shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.coord.length>0)?1:0)+
((!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.coord.length>0)?1:0)+
((!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.normal.length>0)?1:0)+
((!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.texCoord.length>0)?1:0)+
((!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.color.length>0)?1:0);var createTriangleSoup=(shape._cf.geometry.node._vf.normalPerVertex==false)||((shape._cf.geometry.node._vf.indexType=="Uint32")&&(shape._cf.geometry.node._vf.index.length>0));shape._webgl.makeSeparateTris={index:null,coord:null,normal:null,texCoord:null,color:null,pushBuffer:function(name,buf){this[name]=buf;if(--shape._webgl.internalDownloadCount==0){if(this.coord)
this.createMesh();shape._nameSpace.doc.needRender=true;}
if(--shape._nameSpace.doc.downloadCount==0)
shape._nameSpace.doc.needRender=true;},createMesh:function(){var geoNode=shape._cf.geometry.node;if(geoNode._hasStrideOffset){x3dom.debug.logError(geoNode._vf.indexType+" index type and per-face normals not supported for interleaved arrays.");return;}
for(var k=0;k<shape._webgl.primType.length;k++){if(shape._webgl.primType[k]==gl.TRIANGLE_STRIP){x3dom.debug.logError("Triangle strips not yet supported for per-face normals.");return;}}
var attribTypeStr=geoNode._vf.coordType;shape._webgl.coordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);var bgCenter,bgSize,bgPrecisionMax;if(shape._webgl.coordType!=gl.FLOAT)
{if(geoNode._mesh._numPosComponents==4&&x3dom.Utils.isUnsignedType(geoNode._vf.coordType))
bgCenter=x3dom.fields.SFVec3f.copy(geoNode.getMin());else
bgCenter=x3dom.fields.SFVec3f.copy(geoNode._vf.position);bgSize=x3dom.fields.SFVec3f.copy(geoNode._vf.size);bgPrecisionMax=geoNode.getPrecisionMax('coordType');}
else
{bgCenter=new x3dom.fields.SFVec3f(0,0,0);bgSize=new x3dom.fields.SFVec3f(1,1,1);bgPrecisionMax=1.0;}
var dataLen=shape._coordStrideOffset[0]/x3dom.Utils.getDataTypeSize(geoNode._vf.coordType);dataLen=(dataLen==0)?3:dataLen;x3dom.debug.logInfo("makeSeparateTris.createMesh called with coord length "+dataLen);if(this.color&&dataLen!=shape._colorStrideOffset[0]/x3dom.Utils.getDataTypeSize(geoNode._vf.colorType))
{this.color=null;x3dom.debug.logWarning("Color format not supported.");}
var texDataLen=this.texCoord?(shape._texCoordStrideOffset[0]/x3dom.Utils.getDataTypeSize(geoNode._vf.texCoordType)):0;geoNode._vf.normalType="Float32";shape._webgl.normalType=gl.FLOAT;geoNode._mesh._numNormComponents=3;shape._normalStrideOffset=[0,0];var posBuf=[],normBuf=[],texcBuf=[],colBuf=[];var i,j,l,n=this.index?(this.index.length-2):(this.coord.length/3-2);for(i=0;i<n;i+=3)
{j=dataLen*(this.index?this.index[i]:i);var p0=new x3dom.fields.SFVec3f(bgSize.x*this.coord[j]/bgPrecisionMax,bgSize.y*this.coord[j+1]/bgPrecisionMax,bgSize.z*this.coord[j+2]/bgPrecisionMax);posBuf.push(this.coord[j]);posBuf.push(this.coord[j+1]);posBuf.push(this.coord[j+2]);if(dataLen>3)posBuf.push(this.coord[j+3]);if(this.color){colBuf.push(this.color[j]);colBuf.push(this.color[j+1]);colBuf.push(this.color[j+2]);if(dataLen>3)colBuf.push(this.color[j+3]);}
if(this.texCoord){l=texDataLen*(this.index?this.index[i]:i);texcBuf.push(this.texCoord[l]);texcBuf.push(this.texCoord[l+1]);if(texDataLen>3){texcBuf.push(this.texCoord[l+2]);texcBuf.push(this.texCoord[l+3]);}}
j=dataLen*(this.index?this.index[i+1]:i+1);var p1=new x3dom.fields.SFVec3f(bgSize.x*this.coord[j]/bgPrecisionMax,bgSize.y*this.coord[j+1]/bgPrecisionMax,bgSize.z*this.coord[j+2]/bgPrecisionMax);posBuf.push(this.coord[j]);posBuf.push(this.coord[j+1]);posBuf.push(this.coord[j+2]);if(dataLen>3)posBuf.push(this.coord[j+3]);if(this.color){colBuf.push(this.color[j]);colBuf.push(this.color[j+1]);colBuf.push(this.color[j+2]);if(dataLen>3)colBuf.push(this.color[j+3]);}
if(this.texCoord){l=texDataLen*(this.index?this.index[i+1]:i+1);texcBuf.push(this.texCoord[l]);texcBuf.push(this.texCoord[l+1]);if(texDataLen>3){texcBuf.push(this.texCoord[l+2]);texcBuf.push(this.texCoord[l+3]);}}
j=dataLen*(this.index?this.index[i+2]:i+2);var p2=new x3dom.fields.SFVec3f(bgSize.x*this.coord[j]/bgPrecisionMax,bgSize.y*this.coord[j+1]/bgPrecisionMax,bgSize.z*this.coord[j+2]/bgPrecisionMax);posBuf.push(this.coord[j]);posBuf.push(this.coord[j+1]);posBuf.push(this.coord[j+2]);if(dataLen>3)posBuf.push(this.coord[j+3]);if(this.color){colBuf.push(this.color[j]);colBuf.push(this.color[j+1]);colBuf.push(this.color[j+2]);if(dataLen>3)colBuf.push(this.color[j+3]);}
if(this.texCoord){l=texDataLen*(this.index?this.index[i+2]:i+2);texcBuf.push(this.texCoord[l]);texcBuf.push(this.texCoord[l+1]);if(texDataLen>3){texcBuf.push(this.texCoord[l+2]);texcBuf.push(this.texCoord[l+3]);}}
var a=p0.subtract(p1);var b=p1.subtract(p2);var norm=a.cross(b).normalize();for(j=0;j<3;j++){normBuf.push(norm.x);normBuf.push(norm.y);normBuf.push(norm.z);}}
var buffer=gl.createBuffer();shape._webgl.buffers[1]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,x3dom.Utils.getArrayBufferView(geoNode._vf.coordType,posBuf),gl.STATIC_DRAW);gl.vertexAttribPointer(sp.position,geoNode._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);buffer=gl.createBuffer();shape._webgl.buffers[2]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,new Float32Array(normBuf),gl.STATIC_DRAW);gl.vertexAttribPointer(sp.normal,geoNode._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);if(this.texCoord)
{buffer=gl.createBuffer();shape._webgl.buffers[3]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,x3dom.Utils.getArrayBufferView(geoNode._vf.texCoordType,texcBuf),gl.STATIC_DRAW);gl.vertexAttribPointer(sp.texcoord,geoNode._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
if(this.color)
{buffer=gl.createBuffer();shape._webgl.buffers[4]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,x3dom.Utils.getArrayBufferView(geoNode._vf.colorType,colBuf),gl.STATIC_DRAW);gl.vertexAttribPointer(sp.color,geoNode._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}
geoNode._vf.vertexCount=[];geoNode._vf.vertexCount[0]=posBuf.length/dataLen;geoNode._mesh._numCoords=geoNode._vf.vertexCount[0];geoNode._mesh._numFaces=geoNode._vf.vertexCount[0]/3;shape._webgl.primType=[];shape._webgl.primType[0]=gl.TRIANGLES;posBuf=null;normBuf=null;texcBuf=null;colBuf=null;this.index=null;this.coord=null;this.normal=null;this.texCoord=null;this.color=null;delete shape._webgl.shader;shape._webgl.shader=currContext.cache.getDynamicShader(gl,viewarea,shape);}};if(shape._cf.geometry.node._vf.index.length>0)
{var xmlhttp0=new XMLHttpRequest();xmlhttp0.open("GET",encodeURI(shape._nameSpace.getURL(shape._cf.geometry.node._vf.index)),true);xmlhttp0.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttp0.send(null);xmlhttp0.onload=function()
{if(!shape._webgl)
return;var XHR_buffer=xmlhttp0.response;var geoNode=shape._cf.geometry.node;var attribTypeStr=geoNode._vf.indexType;var indexArray=x3dom.Utils.getArrayBufferView(attribTypeStr,XHR_buffer);if(createTriangleSoup){shape._webgl.makeSeparateTris.pushBuffer("index",indexArray);return;}
var indicesBuffer=gl.createBuffer();shape._webgl.buffers[0]=indicesBuffer;gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,indicesBuffer);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,indexArray,gl.STATIC_DRAW);shape._webgl.binaryGeometry=1;if(geoNode._vf.vertexCount[0]==0)
geoNode._vf.vertexCount[0]=indexArray.length;geoNode._mesh._numFaces=0;for(var i=0;i<geoNode._vf.vertexCount.length;i++){if(shape._webgl.primType[i]==gl.TRIANGLE_STRIP)
geoNode._mesh._numFaces+=geoNode._vf.vertexCount[i]-2;else
geoNode._mesh._numFaces+=geoNode._vf.vertexCount[i]/3;}
indexArray=null;shape._nameSpace.doc.downloadCount-=1;shape._webgl.internalDownloadCount-=1;if(shape._webgl.internalDownloadCount==0)
shape._nameSpace.doc.needRender=true;var t11=new Date().getTime()-t00;x3dom.debug.logInfo("XHR0/ index load time: "+t11+" ms");};}
if(shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.coord.length>0)
{var xmlhttp=new XMLHttpRequest();xmlhttp.open("GET",encodeURI(shape._nameSpace.getURL(shape._cf.geometry.node._vf.coord)),true);xmlhttp.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttp.send(null);xmlhttp.onload=function()
{if(!shape._webgl)
return;var XHR_buffer=xmlhttp.response;var geoNode=shape._cf.geometry.node;var attribTypeStr=geoNode._vf.coordType;shape._webgl.coordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._webgl.normalType=shape._webgl.coordType;shape._webgl.texCoordType=shape._webgl.coordType;shape._webgl.colorType=shape._webgl.coordType;var attributes=x3dom.Utils.getArrayBufferView(attribTypeStr,XHR_buffer);var dataLen=shape._coordStrideOffset[0]/x3dom.Utils.getDataTypeSize(attribTypeStr);if(dataLen)
geoNode._mesh._numCoords=attributes.length/dataLen;if(geoNode._vf.index.length==0){for(var i=0;i<geoNode._vf.vertexCount.length;i++){if(shape._webgl.primType[i]==gl.TRIANGLE_STRIP)
geoNode._mesh._numFaces+=geoNode._vf.vertexCount[i]-2;else
geoNode._mesh._numFaces+=geoNode._vf.vertexCount[i]/3;}}
var buffer=gl.createBuffer();shape._webgl.buffers[1]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,attributes,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.position,geoNode._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);if(geoNode._vf.normal.length>0)
{shape._webgl.buffers[2]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,attributes,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.normal,geoNode._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);}
if(geoNode._vf.texCoord.length>0)
{shape._webgl.buffers[3]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,attributes,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.texcoord,geoNode._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
if(geoNode._vf.color.length>0)
{shape._webgl.buffers[4]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,attributes,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.color,geoNode._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}
attributes=null;shape._nameSpace.doc.downloadCount-=1;shape._webgl.internalDownloadCount-=1;if(shape._webgl.internalDownloadCount==0)
shape._nameSpace.doc.needRender=true;var t11=new Date().getTime()-t00;x3dom.debug.logInfo("XHR/ interleaved array load time: "+t11+" ms");};}
if(!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.coord.length>0)
{var xmlhttp1=new XMLHttpRequest();xmlhttp1.open("GET",encodeURI(shape._nameSpace.getURL(shape._cf.geometry.node._vf.coord)),true);xmlhttp1.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttp1.send(null);xmlhttp1.onload=function()
{if(!shape._webgl)
return;var XHR_buffer=xmlhttp1.response;var geoNode=shape._cf.geometry.node;var attribTypeStr=geoNode._vf.coordType;shape._webgl.coordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);var vertices=x3dom.Utils.getArrayBufferView(attribTypeStr,XHR_buffer);if(createTriangleSoup){shape._webgl.makeSeparateTris.pushBuffer("coord",vertices);return;}
var positionBuffer=gl.createBuffer();shape._webgl.buffers[1]=positionBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.bufferData(gl.ARRAY_BUFFER,vertices,gl.STATIC_DRAW);gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.vertexAttribPointer(sp.position,geoNode._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);geoNode._mesh._numCoords=vertices.length/geoNode._mesh._numPosComponents;if(geoNode._vf.index.length==0){for(var i=0;i<geoNode._vf.vertexCount.length;i++){if(shape._webgl.primType[i]==gl.TRIANGLE_STRIP)
geoNode._mesh._numFaces+=geoNode._vf.vertexCount[i]-2;else
geoNode._mesh._numFaces+=geoNode._vf.vertexCount[i]/3;}}
if((attribTypeStr=="Float32")&&(shape._vf.bboxSize.x<0||shape._vf.bboxSize.y<0||shape._vf.bboxSize.z<0))
{var min=new x3dom.fields.SFVec3f(vertices[0],vertices[1],vertices[2]);var max=new x3dom.fields.SFVec3f(vertices[0],vertices[1],vertices[2]);for(var i=3;i<vertices.length;i+=3)
{if(min.x>vertices[i+0]){min.x=vertices[i+0];}
if(min.y>vertices[i+1]){min.y=vertices[i+1];}
if(min.z>vertices[i+2]){min.z=vertices[i+2];}
if(max.x<vertices[i+0]){max.x=vertices[i+0];}
if(max.y<vertices[i+1]){max.y=vertices[i+1];}
if(max.z<vertices[i+2]){max.z=vertices[i+2];}}
shape._vf.bboxCenter.setValues(min.add(max).multiply(0.5));shape._vf.bboxSize.setValues(max.subtract(min));}
vertices=null;shape._nameSpace.doc.downloadCount-=1;shape._webgl.internalDownloadCount-=1;if(shape._webgl.internalDownloadCount==0)
shape._nameSpace.doc.needRender=true;var t11=new Date().getTime()-t00;x3dom.debug.logInfo("XHR1/ coord load time: "+t11+" ms");};}
if(!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.normal.length>0)
{var xmlhttp2=new XMLHttpRequest();xmlhttp2.open("GET",encodeURI(shape._nameSpace.getURL(shape._cf.geometry.node._vf.normal)),true);xmlhttp2.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttp2.send(null);xmlhttp2.onload=function()
{if(!shape._webgl)
return;var XHR_buffer=xmlhttp2.response;var attribTypeStr=shape._cf.geometry.node._vf.normalType;shape._webgl.normalType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);var normals=x3dom.Utils.getArrayBufferView(attribTypeStr,XHR_buffer);if(createTriangleSoup){shape._webgl.makeSeparateTris.pushBuffer("normal",normals);return;}
var normalBuffer=gl.createBuffer();shape._webgl.buffers[2]=normalBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,normalBuffer);gl.bufferData(gl.ARRAY_BUFFER,normals,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.normal,shape._cf.geometry.node._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);normals=null;shape._nameSpace.doc.downloadCount-=1;shape._webgl.internalDownloadCount-=1;if(shape._webgl.internalDownloadCount==0)
shape._nameSpace.doc.needRender=true;var t11=new Date().getTime()-t00;x3dom.debug.logInfo("XHR2/ normal load time: "+t11+" ms");};}
if(!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.texCoord.length>0)
{var xmlhttp3=new XMLHttpRequest();xmlhttp3.open("GET",encodeURI(shape._nameSpace.getURL(shape._cf.geometry.node._vf.texCoord)),true);xmlhttp3.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttp3.send(null);xmlhttp3.onload=function()
{if(!shape._webgl)
return;var XHR_buffer=xmlhttp3.response;var attribTypeStr=shape._cf.geometry.node._vf.texCoordType;shape._webgl.texCoordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);var texCoords=x3dom.Utils.getArrayBufferView(attribTypeStr,XHR_buffer);if(createTriangleSoup){shape._webgl.makeSeparateTris.pushBuffer("texCoord",texCoords);return;}
var texcBuffer=gl.createBuffer();shape._webgl.buffers[3]=texcBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,texcBuffer);gl.bufferData(gl.ARRAY_BUFFER,texCoords,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.texcoord,shape._cf.geometry.node._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);texCoords=null;shape._nameSpace.doc.downloadCount-=1;shape._webgl.internalDownloadCount-=1;if(shape._webgl.internalDownloadCount==0)
shape._nameSpace.doc.needRender=true;var t11=new Date().getTime()-t00;x3dom.debug.logInfo("XHR3/ texCoord load time: "+t11+" ms");};}
if(!shape._cf.geometry.node._hasStrideOffset&&shape._cf.geometry.node._vf.color.length>0)
{var xmlhttp4=new XMLHttpRequest();xmlhttp4.open("GET",encodeURI(shape._nameSpace.getURL(shape._cf.geometry.node._vf.color)),true);xmlhttp4.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttp4.send(null);xmlhttp4.onload=function()
{if(!shape._webgl)
return;var XHR_buffer=xmlhttp4.response;var attribTypeStr=shape._cf.geometry.node._vf.colorType;shape._webgl.colorType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);var colors=x3dom.Utils.getArrayBufferView(attribTypeStr,XHR_buffer);if(createTriangleSoup){shape._webgl.makeSeparateTris.pushBuffer("color",colors);return;}
var colorBuffer=gl.createBuffer();shape._webgl.buffers[4]=colorBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,colorBuffer);gl.bufferData(gl.ARRAY_BUFFER,colors,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.color,shape._cf.geometry.node._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);colors=null;shape._nameSpace.doc.downloadCount-=1;shape._webgl.internalDownloadCount-=1;if(shape._webgl.internalDownloadCount==0)
shape._nameSpace.doc.needRender=true;var t11=new Date().getTime()-t00;x3dom.debug.logInfo("XHR4/ color load time: "+t11+" ms");};}};x3dom.BinaryContainerLoader.setupPopGeo=function(shape,sp,gl,viewarea,currContext)
{var popGeo=shape._cf.geometry.node;if(popGeo.hasIndex()){shape._webgl.popGeometry=1;shape._webgl.buffers[0]=gl.createBuffer();gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,shape._webgl.buffers[0]);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,popGeo.getTotalNumberOfIndices()*2,gl.STATIC_DRAW);shape._webgl.buffers[5]=gl.createBuffer();var idBuffer=new Float32Array(popGeo._vf.vertexBufferSize);(function(){for(var i=0;i<idBuffer.length;++i)idBuffer[i]=i;})();gl.bindBuffer(gl.ARRAY_BUFFER,shape._webgl.buffers[5]);gl.bufferData(gl.ARRAY_BUFFER,idBuffer,gl.STATIC_DRAW);}
else{shape._webgl.popGeometry=-1;}
shape._webgl.buffers[1]=gl.createBuffer();gl.bindBuffer(gl.ARRAY_BUFFER,shape._webgl.buffers[1]);gl.bufferData(gl.ARRAY_BUFFER,(popGeo._vf.attributeStride*popGeo._vf.vertexBufferSize),gl.STATIC_DRAW);var attribTypeStr=popGeo._vf.coordType;shape._webgl.coordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._coordStrideOffset[0]=popGeo.getAttributeStride();shape._coordStrideOffset[1]=popGeo.getPositionOffset();gl.vertexAttribPointer(sp.position,shape._cf.geometry.node._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);if(popGeo.hasNormal()){attribTypeStr=popGeo._vf.normalType;shape._webgl.normalType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._normalStrideOffset[0]=popGeo.getAttributeStride();shape._normalStrideOffset[1]=popGeo.getNormalOffset();shape._webgl.buffers[2]=shape._webgl.buffers[1];gl.vertexAttribPointer(sp.normal,shape._cf.geometry.node._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);}
if(popGeo.hasTexCoord()){attribTypeStr=popGeo._vf.texCoordType;shape._webgl.texCoordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._webgl.buffers[3]=shape._webgl.buffers[1];shape._texCoordStrideOffset[0]=popGeo.getAttributeStride();shape._texCoordStrideOffset[1]=popGeo.getTexCoordOffset();gl.vertexAttribPointer(sp.texcoord,shape._cf.geometry.node._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
if(popGeo.hasColor()){attribTypeStr=popGeo._vf.colorType;shape._webgl.colorType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._webgl.buffers[4]=shape._webgl.buffers[1];shape._colorStrideOffset[0]=popGeo.getAttributeStride();shape._colorStrideOffset[1]=popGeo.getColorOffset();gl.vertexAttribPointer(sp.color,shape._cf.geometry.node._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}
shape._webgl.currentNumIndices=0;shape._webgl.currentNumVertices=0;shape._webgl.numVerticesAtLevel=[];shape._webgl.levelsAvailable=0;shape._webgl.levelLoaded=[];(function(){for(var i=0;i<popGeo.getNumLevels();++i)
shape._webgl.levelLoaded.push(false);})();var uploadDataToGPU=function(data,lvl){shape._webgl.levelLoaded[lvl]=true;shape._webgl.numVerticesAtLevel[lvl]=0;if(data){var indexDataLengthInBytes=0;var redrawNeeded=false;if(popGeo.hasIndex()){indexDataLengthInBytes=popGeo.getNumIndicesByLevel(lvl)*2;if(indexDataLengthInBytes>0){redrawNeeded=true;var indexDataView=new Uint8Array(data,0,indexDataLengthInBytes);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,shape._webgl.buffers[0]);(function(){var indexDataOffset=0;for(var i=0;i<lvl;++i){indexDataOffset+=popGeo.getNumIndicesByLevel(i);}
gl.bufferSubData(gl.ELEMENT_ARRAY_BUFFER,indexDataOffset*2,indexDataView);})();}}
var vertexDataLengthInBytes=data.byteLength-indexDataLengthInBytes;if(vertexDataLengthInBytes>0){redrawNeeded=true;var attributeDataView=new Uint8Array(data,indexDataLengthInBytes,vertexDataLengthInBytes);gl.bindBuffer(gl.ARRAY_BUFFER,shape._webgl.buffers[1]);if(!popGeo.hasIndex()){gl.bufferSubData(gl.ARRAY_BUFFER,shape._webgl.currentNumVertices*popGeo.getAttributeStride(),attributeDataView);}
else{gl.bufferSubData(gl.ARRAY_BUFFER,popGeo.getVertexDataBufferOffset(lvl)*popGeo.getAttributeStride(),attributeDataView);}
shape._webgl.numVerticesAtLevel[lvl]=vertexDataLengthInBytes/popGeo.getAttributeStride();shape._webgl.currentNumVertices+=shape._webgl.numVerticesAtLevel[lvl];}
(function(){var numValidIndices=0;for(var i=shape._webgl.levelsAvailable;i<popGeo.getNumLevels();++i){if(shape._webgl.levelLoaded[i]===false){break;}
else{numValidIndices+=popGeo.getNumIndicesByLevel(i);++shape._webgl.levelsAvailable;}}
shape._webgl.currentNumIndices=numValidIndices;})();popGeo._mesh._numCoords=shape._webgl.currentNumVertices;popGeo._mesh._numFaces=(popGeo.hasIndex()?shape._webgl.currentNumIndices:shape._webgl.currentNumVertices)/3;popGeo.adaptVertexCount(popGeo.hasIndex()?popGeo._mesh._numFaces*3:popGeo._mesh._numCoords);if(redrawNeeded){shape._nameSpace.doc.needRender=true;}}};var dataURLs=popGeo.getDataURLs();var downloadCallbacks=[];var priorities=[];shape._webgl.downloadStartTimer=new Date().getTime();for(var i=0;i<dataURLs.length;++i){shape._nameSpace.doc.downloadCount+=1;(function(idx){downloadCallbacks.push(function(data){shape._nameSpace.doc.downloadCount-=1;return uploadDataToGPU(data,idx);});})(i);priorities.push(i);}
x3dom.DownloadManager.get(dataURLs,downloadCallbacks,priorities);};x3dom.BinaryContainerLoader.setupBitLODGeo=function(shape,sp,gl,viewarea,currContext)
{shape._webgl.bitLODGeometry=-1;var bitLODGeometry=shape._cf.geometry.node;var numComponents=bitLODGeometry.getNumComponents();if(numComponents)
{if(bitLODGeometry.hasIndex())
{shape._webgl.generateTriangleBuffer=function(){if(typeof shape._webgl.dataBuffers[0]!='undefined'&&(typeof shape._webgl.dataBuffers[1]!='undefined'||typeof shape._webgl.dataBuffers[3]!='undefined'||typeof shape._webgl.dataBuffers[4]!='undefined'))
{var indexArray=shape._webgl.dataBuffers[0];var read_idx_pos_nor;var read_idx_tc;var read_idx_col;var write_idx;var i;var n_theta=0;var n_phi=0;var accum_cnt=0;var points=[new x3dom.fields.SFVec3f(0,0,0),new x3dom.fields.SFVec3f(0,0,0),new x3dom.fields.SFVec3f(0,0,0)];var nor=new x3dom.fields.SFVec3f(0,0,0);var v1=new x3dom.fields.SFVec3f(0,0,0);var v2=new x3dom.fields.SFVec3f(0,0,0);var coordsNormalsAvailable=(typeof shape._webgl.dataBuffers[1]!='undefined'&&shape._webgl.dataBuffers[1].length>0);var texCoordsAvailable=(typeof shape._webgl.dataBuffers[3]!='undefined'&&shape._webgl.dataBuffers[3].length>0);var colorsAvailable=(typeof shape._webgl.dataBuffers[4]!='undefined'&&shape._webgl.dataBuffers[4].length>0);var posNorEntriesPerElement=(shape._cf.geometry.node._mesh._numNormComponents==2?6:8);var stride=posNorEntriesPerElement+(bitLODGeometry.hasTexCoord()?2:0)+
(bitLODGeometry.hasColor()?4:0);if(typeof shape._webgl.triangleBuffer=='undefined'){shape._webgl.triangleBuffer=new Uint16Array(indexArray.length*stride);}
for(i=0;i<indexArray.length;++i){write_idx=i*stride;if(coordsNormalsAvailable){read_idx_pos_nor=indexArray[i]*6;shape._webgl.triangleBuffer[write_idx]=shape._webgl.dataBuffers[1][read_idx_pos_nor];shape._webgl.triangleBuffer[write_idx+1]=shape._webgl.dataBuffers[1][read_idx_pos_nor+1];shape._webgl.triangleBuffer[write_idx+2]=shape._webgl.dataBuffers[1][read_idx_pos_nor+2];shape._webgl.triangleBuffer[write_idx+3]=0;if(bitLODGeometry._vf.normalPerVertex){shape._webgl.triangleBuffer[write_idx+4]=shape._webgl.dataBuffers[1][read_idx_pos_nor+4];shape._webgl.triangleBuffer[write_idx+5]=shape._webgl.dataBuffers[1][read_idx_pos_nor+5];}
else if(shape._webgl.loadedLevels===8){points[accum_cnt].x=shape._webgl.dataBuffers[1][read_idx_pos_nor];points[accum_cnt].y=shape._webgl.dataBuffers[1][read_idx_pos_nor+1];points[accum_cnt].z=shape._webgl.dataBuffers[1][read_idx_pos_nor+2];if(++accum_cnt===3){v1=points[1].subtract(points[0]);v2=points[2].subtract(points[0]);nor=v1.cross(v2);nor=nor.normalize();nor=nor.add(new x3dom.fields.SFVec3f(1.0,1.0,1.0));nor=nor.multiply(0.5);nor=nor.multiply(shape._cf.geometry.node.getPrecisionMax('normalType'));shape._webgl.triangleBuffer[write_idx+4-stride*2]=nor.x.toFixed(0);shape._webgl.triangleBuffer[write_idx+5-stride*2]=nor.y.toFixed(0);shape._webgl.triangleBuffer[write_idx+6-stride*2]=nor.z.toFixed(0);shape._webgl.triangleBuffer[write_idx+4-stride]=nor.x.toFixed(0);shape._webgl.triangleBuffer[write_idx+5-stride]=nor.y.toFixed(0);shape._webgl.triangleBuffer[write_idx+6-stride]=nor.z.toFixed(0);shape._webgl.triangleBuffer[write_idx+4]=nor.x.toFixed(0);shape._webgl.triangleBuffer[write_idx+5]=nor.y.toFixed(0);shape._webgl.triangleBuffer[write_idx+6]=nor.z.toFixed(0);accum_cnt=0;}}}
write_idx+=posNorEntriesPerElement;if(texCoordsAvailable){read_idx_tc=indexArray[i]*2;shape._webgl.triangleBuffer[write_idx]=shape._webgl.dataBuffers[3][read_idx_tc];shape._webgl.triangleBuffer[write_idx+1]=shape._webgl.dataBuffers[3][read_idx_tc+1];write_idx+=2;}
if(colorsAvailable){read_idx_col=indexArray[i]*4;shape._webgl.triangleBuffer[write_idx]=shape._webgl.dataBuffers[4][read_idx_col];shape._webgl.triangleBuffer[write_idx+1]=shape._webgl.dataBuffers[4][read_idx_col+1];shape._webgl.triangleBuffer[write_idx+2]=shape._webgl.dataBuffers[4][read_idx_col+2];shape._webgl.triangleBuffer[write_idx+3]=0;write_idx+=4;}}
var glBuf=gl.createBuffer();gl.bindBuffer(gl.ARRAY_BUFFER,glBuf);gl.bufferData(gl.ARRAY_BUFFER,shape._webgl.triangleBuffer,gl.STATIC_DRAW);var attribTypeStr=bitLODGeometry._vf.coordType;shape._webgl.coordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._webgl.normalType=shape._webgl.coordType;shape._coordStrideOffset[0]=shape._normalStrideOffset[0]=stride*2;shape._coordStrideOffset[1]=0;shape._normalStrideOffset[1]=8;shape._webgl.buffers[1]=glBuf;shape._webgl.buffers[2]=glBuf;gl.vertexAttribPointer(sp.position,shape._cf.geometry.node._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);gl.vertexAttribPointer(sp.normal,shape._cf.geometry.node._mesh._numNormComponents,shape._webgl.normalType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);if(bitLODGeometry.hasTexCoord()){shape._webgl.texCoordType=shape._webgl.coordType;shape._webgl.buffers[3]=glBuf;shape._texCoordStrideOffset[0]=stride*2;shape._texCoordStrideOffset[1]=posNorEntriesPerElement*2;gl.vertexAttribPointer(sp.texcoord,shape._cf.geometry.node._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
if(bitLODGeometry.hasColor()){shape._webgl.colorType=shape._webgl.coordType;shape._webgl.buffers[4]=glBuf;shape._colorStrideOffset[0]=stride*2;shape._colorStrideOffset[1]=bitLODGeometry.hasTexCoord()?(posNorEntriesPerElement+2)*2:posNorEntriesPerElement*2;gl.vertexAttribPointer(sp.color,shape._cf.geometry.node._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}}};shape._webgl.bitLODGeometry=1;var xmlhttpLOD=new XMLHttpRequest();xmlhttpLOD.open("GET",encodeURI(shape._nameSpace.getURL(bitLODGeometry._vf.index)),true);xmlhttpLOD.responseType="arraybuffer";shape._nameSpace.doc.downloadCount+=1;xmlhttpLOD.send(null);xmlhttpLOD.onload=function()
{var XHR_buffer=xmlhttpLOD.response;var indexArray;if(bitLODGeometry.usesVLCIndices()){(function(){if(typeof shape._webgl.dataBuffers=='undefined')
shape._webgl.dataBuffers=[];shape._webgl.dataBuffers[0]=[];var codes=x3dom.Utils.getArrayBufferView("Uint8",XHR_buffer);var i=0;var b;var delta;var magic_number;var value=0;var vertexIdx=0;var primIdx=0;var lastVal=-1,preLastVal=-1;while(i<codes.length){if(vertexIdx>=shape._cf.geometry.node._vf.vertexCount[primIdx]){++primIdx;vertexIdx=0;}
b=codes[i++];delta=0;magic_number=128;while(b>=128){delta|=b-128;delta<<=7;magic_number<<=7;b=codes[i++];}
delta|=b;magic_number/=2;delta-=magic_number;value=value+delta;if(shape._webgl.primType[primIdx]==gl.TRIANGLE_STRIP){if(vertexIdx<3){shape._webgl.dataBuffers[0].push(value);}
else if((vertexIdx%2)==0){shape._webgl.dataBuffers[0].push(preLastVal);shape._webgl.dataBuffers[0].push(lastVal);shape._webgl.dataBuffers[0].push(value);}
else{shape._webgl.dataBuffers[0].push(lastVal);shape._webgl.dataBuffers[0].push(preLastVal);shape._webgl.dataBuffers[0].push(value);}
preLastVal=lastVal;lastVal=value;}
else{shape._webgl.dataBuffers[0].push(value);}
++vertexIdx;}}());shape._webgl.bitLODGeometry=-1;shape._webgl.generateTriangleBuffer();bitLODGeometry._mesh._numFaces=shape._webgl.dataBuffers[0].length/3;bitLODGeometry._mesh._numCoords=shape._webgl.dataBuffers[0].length;}
else
{var indicesBuffer=gl.createBuffer();shape._webgl.buffers[0]=indicesBuffer;indexArray=x3dom.Utils.getArrayBufferView("Uint16",XHR_buffer);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,indicesBuffer);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,indexArray,gl.STATIC_DRAW);if(bitLODGeometry.getVertexCount(0)==0)
bitLODGeometry.setVertexCount(0,indexArray.length);bitLODGeometry._mesh._numFaces=0;for(var p=0;p<bitLODGeometry.getNumPrimTypes();p++){if(shape._webgl.primType[p]==gl.TRIANGLE_STRIP)
bitLODGeometry._mesh._numFaces+=bitLODGeometry.getVertexCount(p)-2;else
bitLODGeometry._mesh._numFaces+=bitLODGeometry.getVertexCount(p)/3;}}
indexArray=null;shape._nameSpace.doc.downloadCount-=1;shape._nameSpace.doc.needRender=true;};}
function callBack(attributeId,bufferView)
{if(typeof shape._webgl.loadedLevels=='undefined'){shape._webgl.loadedLevels=0;bitLODGeometry.loadedLevels=0;}
shape._webgl.loadedLevels++;bitLODGeometry.loadedLevels++;if(bitLODGeometry.hasIndex()&&bitLODGeometry.usesVLCIndices()){if(typeof shape._webgl.dataBuffers=='undefined')
shape._webgl.dataBuffers=[];if(attributeId===0){shape._webgl.dataBuffers[1]=bufferView;}
else if(attributeId===1){shape._webgl.dataBuffers[3]=bufferView;}
else if(attributeId===2){shape._webgl.dataBuffers[4]=bufferView;}
shape._webgl.generateTriangleBuffer();}
else
{var buffer=gl.createBuffer();if(attributeId===0){var attribTypeStr=bitLODGeometry._vf.coordType;shape._webgl.coordType=x3dom.Utils.getVertexAttribType(attribTypeStr,gl);shape._webgl.normalType=shape._webgl.coordType;var dataLen=shape._coordStrideOffset[0]/x3dom.Utils.getDataTypeSize(attribTypeStr);if(dataLen&&bitLODGeometry._vf.normalPerVertex)
bitLODGeometry._mesh._numCoords=bufferView.length/dataLen;shape._webgl.buffers[1]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,bufferView,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.position,shape._cf.geometry.node._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);shape._webgl.buffers[2]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,bufferView,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.normal,shape._cf.geometry.node._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);}
else if(attributeId===1)
{shape._webgl.texCoordType=shape._webgl.coordType;shape._webgl.buffers[3]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,bufferView,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.texcoord,shape._cf.geometry.node._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
else if(attributeId===2)
{shape._webgl.colorType=shape._webgl.coordType;shape._webgl.buffers[4]=buffer;gl.bindBuffer(gl.ARRAY_BUFFER,buffer);gl.bufferData(gl.ARRAY_BUFFER,bufferView,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.color,shape._cf.geometry.node._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}
bufferView=null;}
shape._nameSpace.doc.needRender=true;shape._webgl.refinementJobManager.continueProcessing(attributeId);}
shape._webgl.refinementJobManager=new x3dom.RefinementJobManager();var numVerts=bitLODGeometry.getNumVertices();var buf=new ArrayBuffer(12*numVerts);var interleavedCoordNormalBuffer=new Uint16Array(buf);shape._webgl.refinementJobManager.addResultBuffer(0,interleavedCoordNormalBuffer);for(var i=0;i<bitLODGeometry.getCoordNormalURLs().length;++i){shape._webgl.refinementJobManager.addRefinementJob(0,i,bitLODGeometry.getCoordNormalURLs()[i],i,callBack,96,[3,2],[6,2],[0,6],[0,64]);}
if(bitLODGeometry.hasTexCoord()){var tBuf=new ArrayBuffer(4*numVerts);var texCoordBuffer=new Uint16Array(tBuf);shape._webgl.refinementJobManager.addResultBuffer(1,texCoordBuffer);for(i=0;i<bitLODGeometry.getTexCoordURLs().length;++i){shape._webgl.refinementJobManager.addRefinementJob(1,i,bitLODGeometry.getTexCoordURLs()[i],i,callBack,32,[2],[8],[0],[0]);}}
if(bitLODGeometry.hasColor()){var cBuf=new ArrayBuffer(6*numVerts);var colorBuffer=new Uint16Array(cBuf);shape._webgl.refinementJobManager.addResultBuffer(2,colorBuffer);for(i=0;i<bitLODGeometry.getColorURLs().length;++i){shape._webgl.refinementJobManager.addRefinementJob(2,i,bitLODGeometry.getColorURLs()[i],i,callBack,48,[3],[6],[0],[0]);}}}};x3dom.BinaryContainerLoader.setupImgGeo=function(shape,sp,gl,viewarea,currContext)
{var imageGeometry=shape._cf.geometry.node;if(imageGeometry.getIndexTexture()){shape._webgl.imageGeometry=1;}else{shape._webgl.imageGeometry=-1;}
shape._cf.geometry.node._dirty.coord=false;shape._cf.geometry.node._dirty.normal=false;shape._cf.geometry.node._dirty.texCoord=false;shape._cf.geometry.node._dirty.color=false;shape._cf.geometry.node._dirty.index=false;if(currContext.IG_PositionBuffer==null){currContext.IG_PositionBuffer=gl.createBuffer();}
shape._webgl.buffers[1]=currContext.IG_PositionBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,currContext.IG_PositionBuffer);vertices=new Float32Array(shape._webgl.positions[0]);gl.bufferData(gl.ARRAY_BUFFER,vertices,gl.STATIC_DRAW);gl.bindBuffer(gl.ARRAY_BUFFER,currContext.IG_PositionBuffer);gl.vertexAttribPointer(sp.position,shape._cf.geometry.node._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);vertices=null;};x3dom.X3DCanvas=function(x3dElem,canvasIdx){var that=this;this.canvasIdx=canvasIdx;this.initContext=function(canvas,forbidMobileShaders,forceMobileShaders)
{x3dom.debug.logInfo("Initializing X3DCanvas for ["+canvas.id+"]");var gl=x3dom.gfx_webgl(canvas,forbidMobileShaders,forceMobileShaders,x3dElem);if(!gl){x3dom.debug.logError("No 3D context found...");this.x3dElem.removeChild(canvas);return null;}
return gl;};this.initFlashContext=function(object){x3dom.debug.logInfo("Initializing X3DObject for ["+object.id+"]");return x3dom.gfx_flash(object);};this.appendParam=function(node,name,value){var param=document.createElement('param');param.setAttribute('name',name);param.setAttribute('value',value);node.appendChild(param);};this.fileExists=function(url){var xhr=new XMLHttpRequest();try{xhr.open("HEAD",url,false);xhr.send(null);return(xhr.status!=404);}catch(e){return true;}};this.detectFlash=function(required,max)
{var required_version=required;var max_version=max;var available_version=0;if(typeof(navigator.plugins["Shockwave Flash"])=="object")
{var description=navigator.plugins["Shockwave Flash"].description;available_version=description.substr(16,(description.indexOf(".",16)-16));}
else if(typeof(ActiveXObject)=="function"){for(var i=10;i<(max_version+1);i++){try{if(typeof(new ActiveXObject("ShockwaveFlash.ShockwaveFlash."+i))=="object"){available_version=i+1;}}
catch(error){}}}
return[available_version,required_version];};this.createInitFailedDiv=function(x3dElem){var div=document.createElement('div');div.setAttribute("id","x3dom-create-init-failed");div.style.width=x3dElem.getAttribute("width");div.style.height=x3dElem.getAttribute("height");div.style.backgroundColor="#C00";div.style.color="#FFF";div.style.fontSize="20px";div.style.fontWidth="bold";div.style.padding="10px 10px 10px 10px";div.style.display="inline-block";div.style.fontFamily="Helvetica";div.style.textAlign="center";div.appendChild(document.createTextNode('Your Browser does not support X3DOM'));div.appendChild(document.createElement('br'));div.appendChild(document.createTextNode('Read more about Browser support on:'));div.appendChild(document.createElement('br'));var link=document.createElement('a');link.setAttribute('href','http://www.x3dom.org/?page_id=9');link.appendChild(document.createTextNode('X3DOM | Browser Support'));div.appendChild(link);altImg=x3dElem.getAttribute("altImg")||null;if(altImg){altImgObj=new Image();altImgObj.src=altImg;div.style.backgroundImage="url("+altImg+")";div.style.backgroundRepeat="no-repeat";div.style.backgroundPosition="50% 50%";}
x3dElem.appendChild(div);x3dom.debug.logError("Your Browser does not support X3DOM!");};this.createFlashObject=function(x3dElem){var result=this.detectFlash(11,11);if(!result[0]||result[0]<result[1]){return null;}else{x3dom.debug.logInfo("Creating FlashObject for (X)3D element...");var id=x3dElem.getAttribute("id");if(id!==null){id="x3dom-"+id+"-object";}else{var index=new Date().getTime();id="x3dom-"+index+"-object";}
var swf_path=x3dElem.getAttribute("swfpath");if(swf_path===null){swf_path="x3dom.swf";}
if(!this.fileExists(swf_path))
{swf_path="http://www.x3dom.org/download/x3dom.swf";x3dom.debug.logWarning("Can't find local x3dom.swf. X3DOM now using the online version from x3dom.org.");}
var width=x3dElem.getAttribute("width");var idx=-1;if(width==null){width=550;}else{idx=width.indexOf("px");if(idx!=-1){width=width.substr(0,idx);}}
var height=x3dElem.getAttribute("height");if(height==null){height=400;}else{idx=height.indexOf("px");if(idx!=-1){height=height.substr(0,idx);}}
var obj=document.createElement('object');obj.setAttribute('width',width);obj.setAttribute('height',height);obj.setAttribute('id',id);this.appendParam(obj,'menu','false');this.appendParam(obj,'quality','high');this.appendParam(obj,'wmode','gpu');this.appendParam(obj,'allowScriptAccess','always');this.appendParam(obj,'flashvars','width='+width+'&height='+height+'&canvasIdx='+this.canvasIdx);this.appendParam(obj,'movie',swf_path);x3dElem.appendChild(obj);if(navigator.appName=="Microsoft Internet Explorer")
obj.setAttribute('classid','clsid:d27cdb6e-ae6d-11cf-96b8-444553540000');else{obj.setAttribute('type','application/x-shockwave-flash');obj.setAttribute('data',swf_path);}
return obj;}};this.createHTMLCanvas=function(x3dElem)
{x3dom.debug.logInfo("Creating canvas for (X)3D element...");var canvas=document.createElement('canvas');canvas.setAttribute("class","x3dom-canvas");var userStyle=x3dElem.getAttribute("style");if(userStyle){x3dom.debug.logInfo("Inline X3D styles detected");}
var evtArr=["onmousedown","onmousemove","onmouseout","onmouseover","onmouseup","onclick","ondblclick","onkeydown","onkeypress","onkeyup","ontouchstart","ontouchmove","ontouchend","ontouchcancel","ontouchleave","ontouchenter","ongesturestart","ongesturechange","ongestureend","MozTouchDown","MozTouchMove","MozTouchUp"];for(var i=0;i<evtArr.length;i++)
{var evtName=evtArr[i];var userEvt=x3dElem.getAttribute(evtName);if(userEvt){x3dom.debug.logInfo(evtName+", "+userEvt);canvas.setAttribute(evtName,userEvt);}}
if(!x3dElem.__addEventListener&&!x3dElem.__removeEventListener)
{x3dElem.__addEventListener=x3dElem.addEventListener;x3dElem.__removeEventListener=x3dElem.removeEventListener;x3dElem.addEventListener=function(type,func,phase){var j,found=false;for(j=0;j<evtArr.length&&!found;j++){if(evtArr[j]===type){found=true;}}
if(found){x3dom.debug.logInfo('addEventListener for div.on'+type);that.canvas.addEventListener(type,func,phase);}else{x3dom.debug.logInfo('addEventListener for X3D.on'+type);this.__addEventListener(type,func,phase);}};x3dElem.removeEventListener=function(type,func,phase){var j,found=false;for(j=0;j<evtArr.length&&!found;j++){if(evtArr[j]===type){found=true;}}
if(found){x3dom.debug.logInfo('removeEventListener for div.on'+type);that.canvas.removeEventListener(type,func,phase);}else{x3dom.debug.logInfo('removeEventListener for X3D.on'+type);this.__removeEventListener(type,func,phase);}};}
x3dElem.appendChild(canvas);var id=x3dElem.getAttribute("id");if(id!==null){canvas.id="x3dom-"+id+"-canvas";}else{var index=new Date().getTime();canvas.id="x3dom-"+index+"-canvas";}
var w=2,h=2;if((w=x3dElem.getAttribute("width"))!==null){if(w.indexOf("%")>=0){x3dom.debug.logWarning("The width attribute is to be specified in pixels.");}
canvas.style.width=w;canvas.setAttribute("width",w);}
if((h=x3dElem.getAttribute("height"))!==null){if(h.indexOf("%")>=0){x3dom.debug.logWarning("The height attribute is to be specified in pixels.");}
canvas.style.height=h;canvas.setAttribute("height",h);}
canvas.setAttribute("tabindex","0");return canvas;};var _old_dim=[0,0];this.watchForResize=function(){var new_dim=[x3dom.getStyle(that.canvas,"width"),x3dom.getStyle(that.canvas,"height")];if((_old_dim[0]!=new_dim[0])||(_old_dim[1]!=new_dim[1])){_old_dim=new_dim;that.x3dElem.setAttribute("width",new_dim[0]);that.x3dElem.setAttribute("height",new_dim[1]);}};this.createProgressDiv=function(){var progressDiv=document.createElement('div');progressDiv.setAttribute("class","x3dom-progress");var _text=document.createElement('strong');_text.appendChild(document.createTextNode('Loading...'));progressDiv.appendChild(_text);var _inner=document.createElement('span');_inner.setAttribute('style',"width: 25%;");_inner.appendChild(document.createTextNode(' '));progressDiv.appendChild(_inner);progressDiv.oncontextmenu=progressDiv.onmousedown=function(evt){evt.preventDefault();evt.stopPropagation();evt.returnValue=false;return false;};return progressDiv;};this.isFlashReady=false;this.x3dElem=x3dElem;this.backend=this.x3dElem.getAttribute('backend');if(this.backend)
this.backend=this.backend.toLowerCase()
else
this.backend='none';if(this.backend=='flash'){this.backend='flash';this.canvas=this.createFlashObject(x3dElem);if(this.canvas!=null){this.canvas.parent=this;this.gl=this.initFlashContext(this.canvas);}else{this.createInitFailedDiv(x3dElem);return;}}else{this.canvas=this.createHTMLCanvas(x3dElem);this.canvas.parent=this;this.gl=this.initContext(this.canvas,(this.backend.search("desktop")>=0),(this.backend.search("mobile")>=0));this.backend='webgl';if(this.gl==null)
{x3dom.debug.logInfo("Fallback to Flash Renderer");this.backend='flash';this.canvas=this.createFlashObject(x3dElem);if(this.canvas!=null){this.canvas.parent=this;this.gl=this.initFlashContext(this.canvas);}else{this.createInitFailedDiv(x3dElem);return;}}}
x3dom.caps.BACKEND=this.backend;this.lastTimeFPSWasTaken=0;this.framesSinceLastTime=0;this.fps_t0=new Date().getTime();this.doc=null;x3dElem.__setAttribute=x3dElem.setAttribute;x3dElem.setAttribute=function(attrName,newVal){this.__setAttribute(attrName,newVal);switch(attrName){case"width":that.canvas.setAttribute("width",newVal);if(that.doc._viewarea){that.doc._viewarea._width=parseInt(that.canvas.getAttribute("width"),0);}
break;case"height":that.canvas.setAttribute("height",newVal);if(that.doc._viewarea){that.doc._viewarea._height=parseInt(that.canvas.getAttribute("height"),0);}
break;default:}
that.doc.needRender=true;};var runtimeEnabled=x3dElem.getAttribute("runtimeEnabled");if(runtimeEnabled!==null){this.hasRuntime=(runtimeEnabled.toLowerCase()=="true");}else{this.hasRuntime=x3dElem.hasRuntime;}
if(this.gl===null){this.hasRuntime=false;}
if(this.backend!="flash"){this.showStat=x3dElem.getAttribute("showStat");this.stateViewer=new x3dom.States(x3dElem);if(this.showStat!==null&&this.showStat=="true"){this.stateViewer.display(true);}
this.x3dElem.appendChild(this.stateViewer.viewer);}
this.showProgress=x3dElem.getAttribute("showProgress");this.progressDiv=this.createProgressDiv();this.progressDiv.style.display=(this.showProgress!==null&&this.showProgress=="true")?"inline":"none";this.x3dElem.appendChild(this.progressDiv);this.showTouchpoints=x3dElem.getAttribute("showTouchpoints");this.showTouchpoints=this.showTouchpoints?!(this.showTouchpoints.toLowerCase()=="false"):true;this.disableTouch=x3dElem.getAttribute("disableTouch");this.disableTouch=this.disableTouch?(this.disableTouch.toLowerCase()=="true"):false;if(this.canvas!==null&&this.gl!==null&&this.hasRuntime&&this.backend!=="flash"){this.canvas.mouse_dragging=false;this.canvas.mouse_button=0;this.canvas.mouse_drag_x=0;this.canvas.mouse_drag_y=0;this.canvas.isMulti=false;this.canvas.oncontextmenu=function(evt){evt.preventDefault();evt.stopPropagation();evt.returnValue=false;return false;};this.canvas.addEventListener("webglcontextlost",function(event){x3dom.debug.logWarning("WebGL context lost");event.preventDefault();},false);this.canvas.addEventListener("webglcontextrestored",function(event){x3dom.debug.logError("recover WebGL state and resources on context lost NYI");event.preventDefault();},false);this.canvas.addEventListener('mousedown',function(evt){if(!this.isMulti){this.focus();switch(evt.button){case 0:this.mouse_button=1;break;case 1:this.mouse_button=4;break;case 2:this.mouse_button=2;break;default:this.mouse_button=0;break;}
if(evt.shiftKey){this.mouse_button=1;}
if(evt.ctrlKey){this.mouse_button=4;}
if(evt.altKey){this.mouse_button=2;}
var pos=this.parent.mousePosition(evt);this.mouse_drag_x=pos.x;this.mouse_drag_y=pos.y;this.mouse_dragging=true;this.parent.doc.onMousePress(that.gl,this.mouse_drag_x,this.mouse_drag_y,this.mouse_button);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('mouseup',function(evt){if(!this.isMulti){this.mouse_button=0;this.mouse_dragging=false;this.parent.doc.onMouseRelease(that.gl,this.mouse_drag_x,this.mouse_drag_y,this.mouse_button);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('mouseover',function(evt){if(!this.isMulti){this.mouse_button=0;this.mouse_dragging=false;this.parent.doc.onMouseOver(that.gl,this.mouse_drag_x,this.mouse_drag_y,this.mouse_button);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('mouseout',function(evt){if(!this.isMulti){this.mouse_button=0;this.mouse_dragging=false;this.parent.doc.onMouseOut(that.gl,this.mouse_drag_x,this.mouse_drag_y,this.mouse_button);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('dblclick',function(evt){if(!this.isMulti){this.mouse_button=0;var pos=this.parent.mousePosition(evt);this.mouse_drag_x=pos.x;this.mouse_drag_y=pos.y;this.mouse_dragging=false;this.parent.doc.onDoubleClick(that.gl,this.mouse_drag_x,this.mouse_drag_y);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('mousemove',function(evt){if(!this.isMulti){if(evt.shiftKey){this.mouse_button=1;}
if(evt.ctrlKey){this.mouse_button=4;}
if(evt.altKey){this.mouse_button=2;}
var pos=this.parent.mousePosition(evt);this.mouse_drag_x=pos.x;this.mouse_drag_y=pos.y;if(this.mouse_dragging){this.parent.doc.onDrag(that.gl,this.mouse_drag_x,this.mouse_drag_y,this.mouse_button);}
else{this.parent.doc.onMove(that.gl,this.mouse_drag_x,this.mouse_drag_y,this.mouse_button);}
this.parent.doc.needRender=true;evt.preventDefault();evt.stopPropagation();evt.returnValue=false;}},false);this.canvas.addEventListener('DOMMouseScroll',function(evt){if(!this.isMulti){this.mouse_drag_y+=2*evt.detail;this.parent.doc.onDrag(that.gl,this.mouse_drag_x,this.mouse_drag_y,2);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('mousewheel',function(evt){if(!this.isMulti){this.mouse_drag_y-=0.1*evt.wheelDeltaY;this.parent.doc.onDrag(that.gl,this.mouse_drag_x,this.mouse_drag_y,2);this.parent.doc.needRender=true;evt.returnValue=true;}},false);this.canvas.addEventListener('keypress',function(evt){var keysEnabled=this.parent.x3dElem.getAttribute("keysEnabled");if(!keysEnabled||keysEnabled.toLowerCase()=="true"){this.parent.doc.onKeyPress(evt.charCode);}
this.parent.doc.needRender=true;evt.returnValue=true;},true);this.canvas.addEventListener('keyup',function(evt){var keysEnabled=this.parent.x3dElem.getAttribute("keysEnabled");if(!keysEnabled||keysEnabled.toLowerCase()=="true"){this.parent.doc.onKeyUp(evt.keyCode);}
this.parent.doc.needRender=true;evt.returnValue=true;},true);this.canvas.addEventListener('keydown',function(evt){var keysEnabled=this.parent.x3dElem.getAttribute("keysEnabled");if(!keysEnabled||keysEnabled.toLowerCase()=="true"){this.parent.doc.onKeyDown(evt.keyCode);}
this.parent.doc.needRender=true;evt.returnValue=true;},true);var touches={numTouches:0,firstTouchTime:new Date().getTime(),firstTouchPoint:new x3dom.fields.SFVec2f(0,0),lastDrag:new x3dom.fields.SFVec2f(),lastMiddle:new x3dom.fields.SFVec2f(),lastDistance:new x3dom.fields.SFVec2f(),lastSquareDistance:0,lastAngle:0,lastLayer:[],calcAngle:function(vector)
{var rotation=vector.normalize().dot(new x3dom.fields.SFVec2f(1,0));rotation=Math.acos(rotation);if(vector.y<0)
rotation=Math.PI+(Math.PI-rotation);return rotation;},disableTouch:this.disableTouch,visMarker:this.showTouchpoints,visMarkerBag:[],visualizeTouches:function(evt,cleanup)
{if(!this.visMarker)
return;var touchBag=[];var marker=null;for(var i=0;i<evt.touches.length;i++){var id=evt.touches[i].identifier||evt.touches[i].streamId;if(!id)id=0;var index=this.visMarkerBag.indexOf(id);if(index>=0){marker=document.getElementById("visMarker"+id);marker.style.left=(evt.touches[i].pageX)+"px";marker.style.top=(evt.touches[i].pageY)+"px";}
else{marker=document.createElement("div");marker.appendChild(document.createTextNode("#"+id));marker.id="visMarker"+id;marker.className="x3dom-touch-marker";document.body.appendChild(marker);index=this.visMarkerBag.length;this.visMarkerBag[index]=id;}
touchBag.push(id);}
for(var j=this.visMarkerBag.length-1;j>=0;j--){var oldId=this.visMarkerBag[j];if(touchBag.indexOf(oldId)<0){this.visMarkerBag.splice(j,1);marker=document.getElementById("visMarker"+oldId);document.body.removeChild(marker);}}}};var mozilla_ids=[];var mozilla_touches={touches:[],preventDefault:function(){}};var touchStartHandler=function(evt,doc)
{this.isMulti=true;evt.preventDefault();touches.visualizeTouches(evt);if(doc==null)
doc=this.parent.doc;touches.lastLayer=[];var i,pos;for(i=0;i<evt.touches.length;i++){pos=this.parent.mousePosition(evt.touches[i]);touches.lastLayer.push(new Array(evt.touches[i].identifier,new x3dom.fields.SFVec2f(pos.x,pos.y)));}
if(touches.numTouches<1&&evt.touches.length==1){touches.numTouches=1;touches.lastDrag=new x3dom.fields.SFVec2f(evt.touches[0].screenX,evt.touches[0].screenY);}
else if(touches.numTouches<2&&evt.touches.length>=2){touches.numTouches=2;var touch0=new x3dom.fields.SFVec2f(evt.touches[0].screenX,evt.touches[0].screenY);var touch1=new x3dom.fields.SFVec2f(evt.touches[1].screenX,evt.touches[1].screenY);var distance=touch1.subtract(touch0);var middle=distance.multiply(0.5).add(touch0);var squareDistance=distance.dot(distance);touches.lastDistance=distance;touches.lastMiddle=middle;touches.lastSquareDistance=squareDistance;touches.lastAngle=touches.calcAngle(distance);}
doc._scene.updateVolume();doc._viewarea._hasTouches=true;for(i=0;i<evt.touches.length;i++){pos=this.parent.mousePosition(evt.touches[i]);doc.onPick(that.gl,pos.x,pos.y);doc._viewarea.prepareEvents(pos.x,pos.y,1,"onmousedown");doc._viewarea._pickingInfo.lastClickObj=doc._viewarea._pickingInfo.pickObj;doc.needRender=true;}};var touchStartHandlerMoz=function(evt)
{this.isMulti=true;evt.preventDefault();var new_id=true;for(var i=0;i<mozilla_ids.length;++i)
if(mozilla_ids[i]==evt.streamId)
new_id=false;if(new_id==true){evt.identifier=evt.streamId;mozilla_ids.push(evt.streamId);mozilla_touches.touches.push(evt);}
touchStartHandler(mozilla_touches,this.parent.doc);};var touchMoveHandler=function(evt,doc)
{evt.preventDefault();touches.visualizeTouches(evt);if(doc==null)
doc=this.parent.doc;var rotMatrix;if(evt.touches.length==1){var currentDrag=new x3dom.fields.SFVec2f(evt.touches[0].screenX,evt.touches[0].screenY);var deltaDrag=currentDrag.subtract(touches.lastDrag);touches.lastDrag=currentDrag;var mx=x3dom.fields.SFMatrix4f.rotationY(deltaDrag.x/100);var my=x3dom.fields.SFMatrix4f.rotationX(deltaDrag.y/100);rotMatrix=mx.mult(my);doc.onMoveView(that.gl,null,rotMatrix);doc.needRender=true;}
else if(evt.touches.length>=2){var touch0=new x3dom.fields.SFVec2f(evt.touches[0].screenX,evt.touches[0].screenY);var touch1=new x3dom.fields.SFVec2f(evt.touches[1].screenX,evt.touches[1].screenY);var distance=touch1.subtract(touch0);var middle=distance.multiply(0.5).add(touch0);var squareDistance=distance.dot(distance);var deltaMiddle=middle.subtract(touches.lastMiddle);var deltaZoom=squareDistance-touches.lastSquareDistance;var deltaMove=new x3dom.fields.SFVec3f(deltaMiddle.x/screen.width,-deltaMiddle.y/screen.height,deltaZoom/(screen.width*screen.height*0.2));var rotation=touches.calcAngle(distance);var angleDelta=touches.lastAngle-rotation;touches.lastAngle=rotation;rotMatrix=x3dom.fields.SFMatrix4f.rotationZ(angleDelta);touches.lastMiddle=middle;touches.lastDistance=distance;touches.lastSquareDistance=squareDistance;doc.onMoveView(that.gl,deltaMove,rotMatrix);doc.needRender=true;}};var touchMoveHandlerMoz=function(evt)
{evt.preventDefault();for(var i=0;i<mozilla_ids.length;++i)
if(mozilla_ids[i]==evt.streamId)
mozilla_touches.touches[i]=evt;touchMoveHandler(mozilla_touches,this.parent.doc);};var touchEndHandler=function(evt,doc)
{this.isMulti=false;evt.preventDefault();touches.visualizeTouches(evt);if(doc==null)
doc=this.parent.doc;if(touches.numTouches==2&&evt.touches.length==1)
touches.lastDrag=new x3dom.fields.SFVec2f(evt.touches[0].screenX,evt.touches[0].screenY);var dblClick=false;if(evt.touches.length<2){if(touches.numTouches==1)
dblClick=true;touches.numTouches=evt.touches.length;}
doc._viewarea._hasTouches=false;for(var i=0;i<touches.lastLayer.length;i++){var pos=touches.lastLayer[i][1];doc.onPick(that.gl,pos.x,pos.y);if(doc._scene._vf.pickMode.toLowerCase()!=="box"){doc._viewarea.prepareEvents(pos.x,pos.y,1,"onmouseup");doc._viewarea._pickingInfo.lastClickObj=doc._viewarea._pickingInfo.pickObj;if(doc._viewarea._pickingInfo.pickObj&&doc._viewarea._pickingInfo.pickObj===doc._viewarea._pickingInfo.lastClickObj){doc._viewarea.prepareEvents(pos.x,pos.y,1,"onclick");}}
else{var line=doc._viewarea.calcViewRay(pos.x,pos.y);var isect=doc._scene.doIntersect(line);var obj=line.hitObject;if(isect&&obj){doc._viewarea._pick.setValues(line.hitPoint);doc._viewarea.checkEvents(obj,pos.x,pos.y,1,"onclick");x3dom.debug.logInfo("Hit '"+obj._xmlNode.localName+"/ "+
obj._DEF+"' at pos "+doc._viewarea._pick);}}}
if(dblClick){var now=new Date().getTime();var dist=touches.firstTouchPoint.subtract(touches.lastDrag).length();if(dist<18&&now-touches.firstTouchTime<180)
doc.onDoubleClick(that.gl,0,0);touches.firstTouchTime=now;touches.firstTouchPoint=touches.lastDrag;}
doc.needRender=true;};var touchEndHandlerMoz=function(evt)
{this.isMulti=false;evt.preventDefault();var remove_index=-1;for(var i=0;i<mozilla_ids.length;++i)
if(mozilla_ids[i]==evt.streamId)
remove_index=i;if(remove_index!=-1)
{mozilla_ids.splice(remove_index,1);mozilla_touches.touches.splice(remove_index,1);}
touchEndHandler(mozilla_touches,this.parent.doc);};if(!this.disableTouch)
{this.canvas.addEventListener('MozTouchDown',touchStartHandlerMoz,true);this.canvas.addEventListener('MozTouchMove',touchMoveHandlerMoz,true);this.canvas.addEventListener('MozTouchUp',touchEndHandlerMoz,true);this.canvas.addEventListener('touchstart',touchStartHandler,true);this.canvas.addEventListener('touchmove',touchMoveHandler,true);this.canvas.addEventListener('touchend',touchEndHandler,true);}}
this.mousePosition=function(evt)
{var convertPoint=window.webkitConvertPointFromNodeToPage;var x=0,y=0;if("getBoundingClientRect"in document.documentElement){var elem=evt.target.offsetParent;var box=elem.getBoundingClientRect();var scrolleft=window.pageXOffset||document.body.scrollLeft;var scrolltop=window.pageYOffset||document.body.scrollTop;var paddingLeft=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('padding-left'));var borderLeftWidth=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('border-left-width'));var paddingTop=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('padding-top'));var borderTopWidth=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('border-top-width'));x=Math.round(evt.pageX-(box.left+paddingLeft+borderLeftWidth+scrolleft));y=Math.round(evt.pageY-(box.top+paddingTop+borderTopWidth+scrolltop));}
else if(convertPoint){var point=convertPoint(evt.target,new WebKitPoint(0,0));x=Math.round(point.x);y=Math.round(point.y);}
else{x3dom.debug.logError('NO getBoundingClientRect, NO webkitConvertPointFromNodeToPage');}
return new x3dom.fields.SFVec2f(x,y);};};x3dom.X3DCanvas.prototype.tick=function()
{var d=new Date().getTime();if((d-this.lastTimeFPSWasTaken)>=1000)
{var that=this;(function(){var diff=d-that.lastTimeFPSWasTaken;that.x3dElem.runtime.fps=that.framesSinceLastTime/(diff/1000);that.x3dElem.runtime.addMeasurement('FPS',that.framesSinceLastTime/(diff/1000));that.framesSinceLastTime=0;that.lastTimeFPSWasTaken=d;})();}
this.framesSinceLastTime++;var fps=1000.0/(d-this.fps_t0);this.fps_t0=d;try{this.doc.advanceTime(d/1000);var animD=new Date().getTime()-d;if(this.doc.needRender){if(this.x3dElem.runtime.isReady==false){this.x3dElem.runtime.ready();this.x3dElem.runtime.isReady=true;}
this.x3dElem.runtime.enterFrame();this.x3dElem.runtime.addMeasurement('ANIM',animD);if(this.backend=='flash'){if(this.isFlashReady){this.canvas.setFPS({fps:fps});this.doc.needRender=false;this.doc.render(this.gl);}}
else{this.doc.needRender=false;this.doc.render(this.gl);}
this.x3dElem.runtime.exitFrame();}
if(this.progressDiv){if(this.doc.downloadCount>0){this.x3dElem.runtime.addInfo("#LOADS:",this.doc.downloadCount);}else{this.x3dElem.runtime.removeInfo("#LOADS:");}
if(this.doc.properties.getProperty("showProgress")!=='false'){if(this.progressDiv){this.progressDiv.childNodes[0].textContent='Loading: '+(+this.doc.downloadCount);if(this.doc.downloadCount>0){this.progressDiv.style.display='inline';}else{this.progressDiv.style.display='none';}
var myThat=this;window.setTimeout(function(){myThat.doc.downloadCount=0;myThat.progressDiv.style.display='none';},1500);}}else{this.progressDiv.style.display='none';}}}catch(e){x3dom.debug.logException(e);throw e;}};x3dom.X3DCanvas.prototype.load=function(uri,sceneElemPos,settings){this.doc=new x3dom.X3DDocument(this.canvas,this.gl,settings);var x3dCanvas=this;this.doc.onload=function(){x3dom.debug.logInfo("loaded '"+uri+"'");if(x3dCanvas.hasRuntime){(function mainloop(){x3dCanvas.watchForResize();x3dCanvas.tick();window.requestAnimFrame(mainloop,x3dCanvas);})();}else{x3dCanvas.tick();}};this.x3dElem.render=function(){if(x3dCanvas.hasRuntime){x3dCanvas.doc.needRender=true;}else{x3dCanvas.doc.render(x3dCanvas.gl);}};this.x3dElem.context=x3dCanvas.gl.ctx3d;this.doc.onerror=function(){alert('Failed to load X3D document');};this.doc.load(uri,sceneElemPos);};x3dom.runtime={};x3dom.Runtime=function(doc,canvas){this.doc=doc;this.canvas=canvas;this.config={};this.isReady=false;this.fps=0;this.states={measurements:[],infos:[]};};x3dom.Runtime.prototype.addMeasurement=function(title,value){this.states.measurements[title]=value;};x3dom.Runtime.prototype.removeMeasurement=function(title){if(this.states.measurements[title]){delete this.states.measurements[title];}};x3dom.Runtime.prototype.addInfo=function(title,value){this.states.infos[title]=value;};x3dom.Runtime.prototype.removeInfo=function(title){delete this.states.infos[title];};x3dom.Runtime.prototype.initialize=function(doc,canvas){this.doc=doc;this.canvas=canvas;this.config={};this.isReady=false;this.fps=0;};x3dom.Runtime.prototype.ready=function(){x3dom.debug.logInfo('System ready.');};x3dom.Runtime.prototype.enterFrame=function(){};x3dom.Runtime.prototype.exitFrame=function(){};x3dom.Runtime.prototype.getActiveBindable=function(typeName){var stacks;var i,current,result;var type;stacks=this.canvas.doc._bindableBag._stacks;result=[];type=x3dom.nodeTypesLC[typeName.toLowerCase()];if(!type){x3dom.debug.logError('No node of type "'+typeName+'" found.');return null;}
for(i=0;i<stacks.length;i++){current=stacks[i].getActive();if(current._xmlNode!==undefined&&x3dom.isa(current,type)){result.push(current);}}
return result[0]?result[0]._xmlNode:null;};x3dom.Runtime.prototype.nextView=function(){var stack=this.canvas.doc._scene.getViewpoint()._stack;if(stack){stack.switchTo('next');}else{x3dom.debug.logError('No valid ViewBindable stack.');}};x3dom.Runtime.prototype.prevView=function(){var stack=this.canvas.doc._scene.getViewpoint()._stack;if(stack){stack.switchTo('prev');}else{x3dom.debug.logError('No valid ViewBindable stack.');}};x3dom.Runtime.prototype.viewpoint=function(){return this.canvas.doc._scene.getViewpoint();};x3dom.Runtime.prototype.viewMatrix=function(){return this.canvas.doc._viewarea.getViewMatrix();};x3dom.Runtime.prototype.projectionMatrix=function(){return this.canvas.doc._viewarea.getProjectionMatrix();};x3dom.Runtime.prototype.getWorldToCameraCoordinatesMatrix=function(){return this.canvas.doc._viewarea.getWCtoCCMatrix();};x3dom.Runtime.prototype.getCameraToWorldCoordinatesMatrix=function(){return this.canvas.doc._viewarea.getCCtoWCMatrix();};x3dom.Runtime.prototype.getViewingRay=function(x,y){return this.canvas.doc._viewarea.calcViewRay(x,y);};x3dom.Runtime.prototype.getWidth=function(){return this.canvas.doc._viewarea._width;};x3dom.Runtime.prototype.getHeight=function(){return this.canvas.doc._viewarea._height;};x3dom.Runtime.prototype.mousePosition=function(event){var pos=this.canvas.mousePosition(event);return[pos.x,pos.y];};x3dom.Runtime.prototype.calcCanvasPos=function(wx,wy,wz){var pnt=new x3dom.fields.SFVec3f(wx,wy,wz);var mat=this.canvas.doc._viewarea.getWCtoCCMatrix();var pos=mat.multFullMatrixPnt(pnt);var w=this.canvas.doc._viewarea._width;var h=this.canvas.doc._viewarea._height;var x=Math.round((pos.x+1)*(w-1)/2);var y=Math.round((h-1)*(1-pos.y)/2);return[x,y];};x3dom.Runtime.prototype.calcPagePos=function(wx,wy,wz){var elem=this.canvas.canvas.offsetParent;if(!elem){x3dom.debug.logError("Can't calc page pos without offsetParent.");return[0,0];}
var canvasPos=elem.getBoundingClientRect();var mousePos=this.calcCanvasPos(wx,wy,wz);var scrollLeft=window.pageXOffset||document.body.scrollLeft;var scrollTop=window.pageYOffset||document.body.scrollTop;var paddingLeft=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('padding-left'));var borderLeftWidth=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('border-left-width'));var paddingTop=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('padding-top'));var borderTopWidth=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('border-top-width'));var x=canvasPos.left+paddingLeft+borderLeftWidth+scrollLeft+mousePos[0];var y=canvasPos.top+paddingTop+borderTopWidth+scrollTop+mousePos[1];return[x,y];};x3dom.Runtime.prototype.calcClientPos=function(wx,wy,wz){var elem=this.canvas.canvas.offsetParent;if(!elem){x3dom.debug.logError("Can't calc client pos without offsetParent.");return[0,0];}
var canvasPos=elem.getBoundingClientRect();var mousePos=this.calcCanvasPos(wx,wy,wz);var paddingLeft=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('padding-left'));var borderLeftWidth=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('border-left-width'));var paddingTop=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('padding-top'));var borderTopWidth=parseFloat(document.defaultView.getComputedStyle(elem,null).getPropertyValue('border-top-width'));var x=canvasPos.left+paddingLeft+borderLeftWidth+mousePos[0];var y=canvasPos.top+paddingTop+borderTopWidth+mousePos[1];return[x,y];};x3dom.Runtime.prototype.getScreenshot=function(){var url="";var backend=this.canvas.backend;var canvas=this.canvas.canvas;if(canvas){if(backend=="flash"){url=canvas.getScreenshot();}else{var canvas2d=document.createElement("canvas");canvas2d.width=canvas.width;canvas2d.height=canvas.height;var ctx=canvas2d.getContext("2d");ctx.drawImage(canvas,0,0,canvas.width,canvas.height);ctx.scale(1,-1);ctx.translate(0,-canvas.height);url=canvas2d.toDataURL();}}
return url;};x3dom.Runtime.prototype.getCanvas=function(){return this.canvas.canvas;};x3dom.Runtime.prototype.lightMatrix=function(){this.canvas.doc._viewarea.getLightMatrix();};x3dom.Runtime.prototype.resetView=function(){this.canvas.doc._viewarea.resetView();};x3dom.Runtime.prototype.lightView=function(){if(this.canvas.doc._nodeBag.lights.length>0){this.canvas.doc._viewarea.animateTo(this.canvas.doc._viewarea.getLightMatrix()[0],this.canvas.doc._scene.getViewpoint());return true;}else{x3dom.debug.logInfo("No lights to navigate to.");return false;}};x3dom.Runtime.prototype.uprightView=function(){this.canvas.doc._viewarea.uprightView();};x3dom.Runtime.prototype.showAll=function(axis){this.canvas.doc._viewarea.showAll(axis);};x3dom.Runtime.prototype.showObject=function(obj){var min=x3dom.fields.SFVec3f.MAX();var max=x3dom.fields.SFVec3f.MIN();if(obj&&obj._x3domNode&&obj._x3domNode.getVolume(min,max))
{var mat=obj._x3domNode.getCurrentTransform();min=mat.multMatrixPnt(min);max=mat.multMatrixPnt(max);var focalLen=(this.canvas.doc._viewarea._width<this.canvas.doc._viewarea._height)?this.canvas.doc._viewarea._width:this.canvas.doc._viewarea._height;var n0=new x3dom.fields.SFVec3f(0,0,1);var viewpoint=this.canvas.doc._scene.getViewpoint();var fov=viewpoint.getFieldOfView()/2.0;var ta=Math.tan(fov);if(Math.abs(ta)>x3dom.fields.Eps){focalLen/=ta;}
var w=this.canvas.doc._viewarea._width-1;var h=this.canvas.doc._viewarea._height-1;var frame=0.25;var minScreenPos=new x3dom.fields.SFVec2f(frame*w,frame*h);frame=0.75;var maxScreenPos=new x3dom.fields.SFVec2f(frame*w,frame*h);var dia2=max.subtract(min).multiply(0.5);var rw=dia2.length();var pc=min.add(dia2);var vc=maxScreenPos.subtract(minScreenPos).multiply(0.5);var rs=1.5*vc.length();vc=vc.add(minScreenPos);var dist=1.0;if(rs>x3dom.fields.Eps){dist=(rw/rs)*Math.sqrt(vc.x*vc.x+vc.y*vc.y+focalLen*focalLen);}
n0=mat.multMatrixVec(n0).normalize();n0=n0.multiply(dist);var p0=pc.add(n0);var qDir=x3dom.fields.Quaternion.rotateFromTo(new x3dom.fields.SFVec3f(0,0,1),n0);var R=qDir.toMatrix();var T=x3dom.fields.SFMatrix4f.translation(p0.negate());var M=x3dom.fields.SFMatrix4f.translation(p0);M=M.mult(R).mult(T).mult(M);var viewmat=M.inverse();this.canvas.doc._viewarea.animateTo(viewmat,viewpoint);}};x3dom.Runtime.prototype.getCenter=function(domNode){if(domNode&&domNode._x3domNode&&(this.isA(domNode,"X3DShapeNode")||this.isA(domNode,"X3DGeometryNode")))
{return domNode._x3domNode.getCenter();}
return null;};x3dom.Runtime.prototype.getCurrentTransform=function(domNode){if(domNode&&domNode._x3domNode)
{return domNode._x3domNode.getCurrentTransform();}
return null;};x3dom.Runtime.prototype.getSceneBBox=function(){var scene=this.canvas.doc._scene;if(!(scene._lastMin&&scene._lastMax))
scene.updateVolume();return{min:x3dom.fields.SFVec3f.copy(scene._lastMin),max:x3dom.fields.SFVec3f.copy(scene._lastMax)}};x3dom.Runtime.prototype.debug=function(show){if(show===true){this.canvas.doc._viewarea._visDbgBuf=true;x3dom.debug.logContainer.style.display="block";}
if(show===false){this.canvas.doc._viewarea._visDbgBuf=false;x3dom.debug.logContainer.style.display="none";}
else{if(this.canvas.doc._viewarea._visDbgBuf===undefined)
this.canvas.doc._viewarea._visDbgBuf=true;else
this.canvas.doc._viewarea._visDbgBuf=!this.canvas.doc._viewarea._visDbgBuf;x3dom.debug.logContainer.style.display=(this.canvas.doc._viewarea._visDbgBuf===true)?"block":"none";}
this.canvas.doc.needRender=true;return this.canvas.doc._viewarea._visDbgBuf;};x3dom.Runtime.prototype.navigationType=function(){return this.canvas.doc._scene.getNavigationInfo().getType();};x3dom.Runtime.prototype.noNav=function(){this.canvas.doc._scene.getNavigationInfo().setType("none");};x3dom.Runtime.prototype.examine=function(){this.canvas.doc._scene.getNavigationInfo().setType("examine");};x3dom.Runtime.prototype.fly=function(){this.canvas.doc._scene.getNavigationInfo().setType("fly");};x3dom.Runtime.prototype.lookAt=function(){this.canvas.doc._scene.getNavigationInfo().setType("lookat");};x3dom.Runtime.prototype.lookAround=function(){this.canvas.doc._scene.getNavigationInfo().setType("lookaround");};x3dom.Runtime.prototype.walk=function(){this.canvas.doc._scene.getNavigationInfo().setType("walk");};x3dom.Runtime.prototype.game=function(){this.canvas.doc._scene.getNavigationInfo().setType("game");};x3dom.Runtime.prototype.helicopter=function(){this.canvas.doc._scene.getNavigationInfo().setType("helicopter");};x3dom.Runtime.prototype.resetExamin=function(){var viewarea=this.canvas.doc._viewarea;viewarea._relMat=x3dom.fields.SFMatrix4f.identity();viewarea._rotMat=x3dom.fields.SFMatrix4f.identity();viewarea._transMat=x3dom.fields.SFMatrix4f.identity();viewarea._movement=new x3dom.fields.SFVec3f(0,0,0);this.canvas.doc.needRender=true;};x3dom.Runtime.prototype.togglePoints=function(){if(this.canvas.doc._viewarea._points===undefined)
this.canvas.doc._viewarea._points=0;this.canvas.doc._viewarea._points=++this.canvas.doc._viewarea._points%2;this.canvas.doc.needRender=true;};x3dom.Runtime.prototype.pickRect=function(x1,y1,x2,y2){return this.canvas.doc.onPickRect(this.canvas.gl,x1,y1,x2,y2);};x3dom.Runtime.prototype.pickMode=function(options){if(options&&options.internal===true){return this.canvas.doc._scene._vf.pickMode;}
return this.canvas.doc._scene._vf.pickMode.toLowerCase();};x3dom.Runtime.prototype.changePickMode=function(type,options){type=type.toLowerCase();switch(type){case'idbuf':type='idBuf';break;case'idbuf24':type='idBuf24';break;case'texcoord':type='texCoord';break;case'color':type='color';break;case'box':type='box';break;default:x3dom.debug.logWarning("Switch pickMode to "+type+' unknown intersect type');type=undefined;}
if(type!==undefined){this.canvas.doc._scene._vf.pickMode=type;x3dom.debug.logInfo("Switched pickMode to '"+type+"'.");return false;}
return true;};x3dom.Runtime.prototype.speed=function(newSpeed){if(newSpeed){this.canvas.doc._scene.getNavigationInfo()._vf.speed=newSpeed;x3dom.debug.logInfo("Changed navigation speed to "+this.canvas.doc._scene.getNavigationInfo()._vf.speed);}
return this.canvas.doc._scene.getNavigationInfo()._vf.speed;};x3dom.Runtime.prototype.statistics=function(mode){var states=this.canvas.stateViewer;if(states){this.canvas.doc.needRender=true;if(mode===true){states.display(mode);return true;}
if(mode===false){states.display(mode);return false;}
return states.active;}};x3dom.Runtime.prototype.processIndicator=function(mode){var processDiv=this.canvas.processDiv;if(processDiv){if(mode===true){processDiv.style.display='inline';return true;}
if(mode===false){processDiv.style.display='none';return false;}
return processDiv.style.display!='none'}};x3dom.Runtime.prototype.properties=function(){return this.canvas.doc.properties;};x3dom.Runtime.prototype.backendName=function(){return this.canvas.backend;};x3dom.Runtime.prototype.getFPS=function(){return this.fps;};x3dom.Runtime.prototype.isA=function(domNode,nodeType){var inherits=false;if(nodeType&&domNode&&domNode._x3domNode){if(nodeType===""){nodeType="X3DNode";}
inherits=x3dom.isa(domNode._x3domNode,x3dom.nodeTypesLC[nodeType.toLowerCase()]);}
return inherits;};x3dom.detectActiveX=function(){var isInstalled=false;if(window.ActiveXObject){var control=null;try{control=new ActiveXObject('AVALONATX.InstantPluginATXCtrl.1');}catch(e){}
if(control){isInstalled=true;}}
return isInstalled;};x3dom.rerouteSetAttribute=function(node,browser){node._setAttribute=node.setAttribute;node.setAttribute=function(name,value){var id=node.getAttribute("_x3domNode");var anode=browser.findNode(id);if(anode)
return anode.parseField(name,value);else
return 0;};for(var i=0;i<node.childNodes.length;i++){var child=node.childNodes[i];x3dom.rerouteSetAttribute(child,browser);}};x3dom.insertActiveX=function(x3d){if(typeof x3dom.atxCtrlCounter=='undefined'){x3dom.atxCtrlCounter=0;}
var height=x3d.getAttribute("height");var width=x3d.getAttribute("width");var parent=x3d.parentNode;var divelem=document.createElement("div");divelem.setAttribute("id","x3dplaceholder");var inserted=parent.insertBefore(divelem,x3d);var hiddenx3d=document.createElement("div");hiddenx3d.style.display="none";parent.appendChild(hiddenx3d);parent.removeChild(x3d);hiddenx3d.appendChild(x3d);var atx=document.createElement("object");var containerName="Avalon"+x3dom.atxCtrlCounter;x3dom.atxCtrlCounter++;atx.setAttribute("id",containerName);atx.setAttribute("classid","CLSID:F3254BA0-99FF-4D14-BD81-EDA9873A471E");atx.setAttribute("width",width?width:"500");atx.setAttribute("height",height?height:"500");inserted.appendChild(atx);var atxctrl=document.getElementById(containerName);var browser=atxctrl.getBrowser();var scene=browser.importDocument(x3d);browser.replaceWorld(scene);x3d.getBrowser=function(){return atxctrl.getBrowser();};x3dom.rerouteSetAttribute(x3d,browser);};x3dom.userAgentFeature={supportsDOMAttrModified:false};(function loadX3DOM(){var onload=function(){var i,j;var x3ds=document.getElementsByTagName('X3D');var w3sg=document.getElementsByTagName('webSG');var params;var settings=new x3dom.Properties();var validParams=array_to_object(['showLog','showStat','showProgress','PrimitiveQuality','components','loadpath','disableDoubleClick','maxActiveDownloads']);var components,prefix;var showLoggingConsole=false;for(i=0;i<x3ds.length;i++){settings.setProperty("showLog",x3ds[i].getAttribute("showLog")||'false');settings.setProperty("showStat",x3ds[i].getAttribute("showStat")||'false');settings.setProperty("showProgress",x3ds[i].getAttribute("showProgress")||'true');settings.setProperty("PrimitiveQuality",x3ds[i].getAttribute("PrimitiveQuality")||'High');params=x3ds[i].getElementsByTagName('PARAM');for(j=0;j<params.length;j++){if(params[j].getAttribute('name')in validParams){settings.setProperty(params[j].getAttribute('name'),params[j].getAttribute('value'));}else{}}
if(settings.getProperty('showLog')==='true'){showLoggingConsole=true;}
if(typeof X3DOM_SECURITY_OFF!='undefined'&&X3DOM_SECURITY_OFF===true){components=settings.getProperty('components',x3ds[i].getAttribute("components"));if(components){prefix=settings.getProperty('loadpath',x3ds[i].getAttribute("loadpath"))
components=components.trim().split(',');for(j=0;j<components.length;j++){x3dom.loadJS(components[j]+".js",prefix);}}}
if(typeof X3DOM_SECURITY_OFF!='undefined'&&X3DOM_SECURITY_OFF===true){if(x3ds[i].getAttribute("src")){var _scene=document.createElement("scene");var _inl=document.createElement("Inline");_inl.setAttribute("url",x3ds[i].getAttribute("src"));_scene.appendChild(_inl);x3ds[i].appendChild(_scene);}}}
if(showLoggingConsole==true){x3dom.debug.activate(true);}else{x3dom.debug.activate(false);}
x3ds=Array.map(x3ds,function(n){n.runtime=new x3dom.Runtime();n.hasRuntime=true;return n;});w3sg=Array.map(w3sg,function(n){n.hasRuntime=false;return n;});for(i=0;i<w3sg.length;i++){x3ds.push(w3sg[i]);}
if(x3dom.versionInfo!==undefined){x3dom.debug.logInfo("X3DOM version "+x3dom.versionInfo.version+", "+"Revison <a href='https://github.com/x3dom/x3dom/tree/"+x3dom.versionInfo.revision+"'>"
+x3dom.versionInfo.revision+"</a>, "+"Date "+x3dom.versionInfo.date);}
x3dom.debug.logInfo("Found "+(x3ds.length-w3sg.length)+" X3D and "+
w3sg.length+" (experimental) WebSG nodes...");var x3d_element;var x3dcanvas;var altDiv,altP,aLnk,altImg,altImgObj;var t0,t1;for(i=0;i<x3ds.length;i++)
{x3d_element=x3ds[i];if(x3dom.detectActiveX()){x3dom.insertActiveX(x3d_element);continue;}
x3dcanvas=new x3dom.X3DCanvas(x3d_element,i);if(x3dcanvas.gl===null){altDiv=document.createElement("div");altDiv.setAttribute("class","x3dom-nox3d");altDiv.setAttribute("id","x3dom-nox3d");altP=document.createElement("p");altP.appendChild(document.createTextNode("WebGL is not yet supported in your browser. "));aLnk=document.createElement("a");aLnk.setAttribute("href","http://www.x3dom.org/?page_id=9");aLnk.appendChild(document.createTextNode("Follow link for a list of supported browsers... "));altDiv.appendChild(altP);altDiv.appendChild(aLnk);x3dcanvas.x3dElem.appendChild(altDiv);if(x3dcanvas.stateViewer){x3d_element.removeChild(x3dcanvas.stateViewer.viewer);}
continue;}
t0=new Date().getTime();x3ds[i].runtime=new x3dom.Runtime(x3ds[i],x3dcanvas);x3ds[i].runtime.initialize(x3ds[i],x3dcanvas);if(x3dom.runtime.ready){x3ds[i].runtime.ready=x3dom.runtime.ready;}
x3dcanvas.load(x3ds[i],i,settings);if(settings.getProperty('showStat')==='true'){x3ds[i].runtime.statistics(true);}else{x3ds[i].runtime.statistics(false);}
if(settings.getProperty('showProgress')==='true'){if(settings.getProperty('showProgress')==='bar'){x3dcanvas.progressDiv.setAttribute("class","x3dom-progress bar");}
x3ds[i].runtime.processIndicator(true);}else{x3ds[i].runtime.processIndicator(false);}
x3dom.canvases.push(x3dcanvas);t1=new Date().getTime()-t0;x3dom.debug.logInfo("Time for setup and init of GL element no. "+i+": "+t1+" ms.");}
var ready=(function(eventType){var evt=null;if(document.createEvent){evt=document.createEvent("Events");evt.initEvent(eventType,true,true);document.dispatchEvent(evt);}else if(document.createEventObject){evt=document.createEventObject();document.body.fireEvent('on'+eventType,evt);}})('load');};var onunload=function(){if(x3dom.canvases){for(var i=0;i<x3dom.canvases.length;i++){x3dom.canvases[i].doc.shutdown(x3dom.canvases[i].gl);}
x3dom.canvases=[];}};x3dom.reload=function(){onunload();onload();};if(navigator.userAgent.indexOf("Chrome")!=-1){document.__getElementsByTagName=document.getElementsByTagName;document.getElementsByTagName=function(tag){var obj=new Array();var elems=this.__getElementsByTagName("*");if(tag=="*"){obj=elems;}else{tag=tag.toUpperCase();for(var i=0;i<elems.length;i++){var tagName=elems[i].tagName.toUpperCase();if(tagName===tag){obj.push(elems[i]);}}}
return obj;};document.__getElementById=document.getElementById;document.getElementById=function(id){var obj=this.__getElementById(id);if(!obj){var elems=this.__getElementsByTagName("*");for(var i=0;i<elems.length&&!obj;i++){if(elems[i].getAttribute("id")===id){obj=elems[i];}}}
return obj;};}else{document.__getElementById=document.getElementById;document.getElementById=function(id){var obj=this.__getElementById(id);if(!obj){var elems=this.getElementsByTagName("*");for(var i=0;i<elems.length&&!obj;i++){if(elems[i].getAttribute("id")===id){obj=elems[i];}}}
return obj;};}
if(window.addEventListener){window.addEventListener('load',onload,false);window.addEventListener('unload',onunload,false);window.addEventListener('reload',onunload,false);}else if(window.attachEvent){window.attachEvent('onload',onload);window.attachEvent('onunload',onunload);window.attachEvent('onreload',onunload);}})();x3dom.Cache=function()
{this.textures=[];this.shaders=[];};x3dom.Cache.prototype.getTexture2D=function(gl,doc,url,bgnd,withCredentials)
{var textureIdentifier=url;if(this.textures[textureIdentifier]===undefined)
{this.textures[textureIdentifier]=x3dom.Utils.createTexture2D(gl,doc,url,bgnd,withCredentials);}
return this.textures[textureIdentifier];};x3dom.Cache.prototype.getTextureCube=function(gl,doc,url,bgnd,withCredentials)
{var textureIdentifier="";for(var i=0;i<url.length;++i)
{textureIdentifier+=url[i]+"|";}
if(this.textures[textureIdentifier]===undefined)
{this.textures[textureIdentifier]=x3dom.Utils.createTextureCube(gl,doc,url,bgnd,withCredentials);}
return this.textures[textureIdentifier];};x3dom.Cache.prototype.getShader=function(gl,shaderIdentifier)
{var program=null;if(this.shaders[shaderIdentifier]===undefined)
{switch(shaderIdentifier)
{case x3dom.shader.PICKING:program=new x3dom.shader.PickingShader(gl);break;case x3dom.shader.PICKING_24:program=new x3dom.shader.Picking24Shader(gl);break;case x3dom.shader.PICKING_COLOR:program=new x3dom.shader.PickingColorShader(gl);break;case x3dom.shader.PICKING_TEXCOORD:program=new x3dom.shader.PickingTexcoordShader(gl);break;case x3dom.shader.FRONTGROUND_TEXTURE:program=new x3dom.shader.FrontgroundTextureShader(gl);break;case x3dom.shader.BACKGROUND_TEXTURE:program=new x3dom.shader.BackgroundTextureShader(gl);break;case x3dom.shader.BACKGROUND_SKYTEXTURE:program=new x3dom.shader.BackgroundSkyTextureShader(gl);break;case x3dom.shader.BACKGROUND_CUBETEXTURE:program=new x3dom.shader.BackgroundCubeTextureShader(gl);break;case x3dom.shader.SHADOW:program=new x3dom.shader.ShadowShader(gl);break;case x3dom.shader.DEPTH:break;case x3dom.shader.NORMAL:program=new x3dom.shader.NormalShader(gl);break;default:break;}
if(program)
this.shaders[shaderIdentifier]=x3dom.Utils.wrapProgram(gl,program);}
return this.shaders[shaderIdentifier];};x3dom.Cache.prototype.getDynamicShader=function(gl,viewarea,shape)
{var properties=x3dom.Utils.generateProperties(viewarea,shape);if(this.shaders[properties.toIdentifier()]===undefined)
{var program;if(properties.CSHADER>=0){program=new x3dom.shader.ComposedShader(gl,shape);}else{program=(x3dom.caps.MOBILE&&!properties.CSSHADER)?new x3dom.shader.DynamicMobileShader(gl,properties):new x3dom.shader.DynamicShader(gl,properties);}
this.shaders[properties.toIdentifier()]=x3dom.Utils.wrapProgram(gl,program);}
return this.shaders[properties.toIdentifier()];};x3dom.Cache.prototype.Release=function()
{for(var texture in this.textures){gl.deleteTexture(this.textures[texture]);}
for(var shader in this.shaders){gl.deleteProgram(this.shaders[shader]);}};x3dom.Texture=function(gl,doc,cache,node)
{this.gl=gl;this.doc=doc;this.cache=cache;this.node=node;this.samplerName="diffuseMap";this.type=gl.TEXTURE_2D;this.format=gl.RGBA;this.magFilter=gl.LINEAR;this.minFilter=gl.LINEAR;this.wrapS=gl.REPEAT;this.wrapT=gl.REPEAT;this.genMipMaps=false;this.texture=null;this.update();};x3dom.Texture.prototype.update=function()
{if(x3dom.isa(this.node,x3dom.nodeTypes.Text))
{this.updateText();}
else
{this.updateTexture();}};x3dom.Texture.prototype.updateTexture=function()
{var gl=this.gl;var doc=this.doc;var tex=this.node;this.samplerName=tex._type;if(x3dom.isa(tex,x3dom.nodeTypes.X3DEnvironmentTextureNode)){this.type=gl.TEXTURE_CUBE_MAP;}else{this.type=gl.TEXTURE_2D;}
if(x3dom.isa(tex,x3dom.nodeTypes.PixelTexture)){switch(tex._vf.image.comp)
{case 1:this.format=gl.LUMINANCE;break;case 2:this.format=gl.LUMINANCE_ALPHA;break;case 3:this.format=gl.RGB;break;case 4:this.format=gl.RGBA;break;}}else{this.format=gl.RGBA;}
var childTex=(tex._video!==undefined&&tex._video!==null&&tex._needPerFrameUpdate!==undefined&&tex._needPerFrameUpdate===true);if(tex._cf.textureProperties.node!==null){var texProp=tex._cf.textureProperties.node;this.wrapS=x3dom.Utils.boundaryModesDic(gl,texProp._vf.boundaryModeS.toUpperCase());this.wrapT=x3dom.Utils.boundaryModesDic(gl,texProp._vf.boundaryModeT.toUpperCase());this.minFilter=x3dom.Utils.minFilterDic(gl,texProp._vf.minificationFilter.toUpperCase());this.magFilter=x3dom.Utils.magFilterDic(gl,texProp._vf.magnificationFilter.toUpperCase());if(texProp._vf.generateMipMaps===true){this.genMipMaps=true;if(this.minFilter==gl.NEAREST){this.minFilter=gl.NEAREST_MIPMAP_NEAREST;}else if(this.minFilter==gl.LINEAR){this.minFilter=gl.LINEAR_MIPMAP_LINEAR;}}else{this.genMipMaps=false;if((this.minFilter==gl.LINEAR_MIPMAP_LINEAR)||(this.minFilter==gl.LINEAR_MIPMAP_NEAREST)){this.minFilter=gl.LINEAR;}else if((this.minFilter==gl.NEAREST_MIPMAP_LINEAR)||(this.minFilter==gl.NEAREST_MIPMAP_NEAREST)){this.minFilter=gl.NEAREST;}}}else{if(tex._vf.repeatS==false){this.wrapS=gl.CLAMP_TO_EDGE;}
if(tex._vf.repeatT==false){this.wrapT=gl.CLAMP_TO_EDGE;}}
if(tex._isCanvas&&tex._canvas)
{if(this.texture==null){this.texture=gl.createTexture()}
gl.bindTexture(this.type,this.texture);gl.texImage2D(this.type,0,this.format,this.format,gl.UNSIGNED_BYTE,tex._canvas);gl.bindTexture(this.type,null);}
else if(x3dom.isa(tex,x3dom.nodeTypes.RenderedTexture))
{if(tex._webgl&&tex._webgl.fbo){this.texture=tex._webgl.fbo.tex;}
else{this.texture=null;x3dom.debug.logError("Try updating RenderedTexture without FBO initialized!");}}
else if(x3dom.isa(tex,x3dom.nodeTypes.PixelTexture))
{if(this.texture==null){this.texture=gl.createTexture()}
var pixelArr=tex._vf.image.toGL();var pixelArrfont_size=tex._vf.image.width*tex._vf.image.height*tex._vf.image.comp;while(pixelArr.length<pixelArrfont_size){pixelArr.push(0);}
var pixels=new Uint8Array(pixelArr);gl.bindTexture(this.type,this.texture);gl.pixelStorei(gl.UNPACK_ALIGNMENT,1);gl.texImage2D(this.type,0,this.format,tex._vf.image.width,tex._vf.image.height,0,this.format,gl.UNSIGNED_BYTE,pixels);gl.bindTexture(this.type,null);}
else if(x3dom.isa(tex,x3dom.nodeTypes.MovieTexture)||childTex)
{var that=this;if(this.texture==null){this.texture=gl.createTexture();}
if(!this.childTex)
{tex._video=document.createElement('video');tex._video.setAttribute('autobuffer','true');var p=document.getElementsByTagName('body')[0];p.appendChild(tex._video);tex._video.style.visibility="hidden";}
for(var i=0;i<tex._vf.url.length;i++)
{var videoUrl=tex._nameSpace.getURL(tex._vf.url[i]);x3dom.debug.logInfo('Adding video file: '+videoUrl);var src=document.createElement('source');src.setAttribute('src',videoUrl);tex._video.appendChild(src);}
var updateMovie=function()
{gl.bindTexture(that.type,that.texture);gl.texImage2D(that.type,0,that.format,that.format,gl.UNSIGNED_BYTE,tex._video);gl.bindTexture(that.type,null);doc.needRender=true;};var startVideo=function()
{tex._video.play();tex._intervalID=setInterval(updateMovie,16);};var videoDone=function()
{clearInterval(tex._intervalID);if(tex._vf.loop===true)
{tex._video.play();tex._intervalID=setInterval(updateMovie,16);}};tex._video.addEventListener("canplaythrough",startVideo,true);tex._video.addEventListener("ended",videoDone,true);}
else if(x3dom.isa(tex,x3dom.nodeTypes.X3DEnvironmentTextureNode))
{this.texture=this.cache.getTextureCube(gl,doc,tex.getTexUrl(),false,tex._vf.withCredentials);}
else
{this.texture=this.cache.getTexture2D(gl,doc,tex._nameSpace.getURL(tex._vf.url[0]),false,tex._vf.withCredentials);}};x3dom.Texture.prototype.updateText=function()
{var gl=this.gl;this.wrapS=gl.CLAMP_TO_EDGE;this.wrapT=gl.CLAMP_TO_EDGE;var fontStyleNode=this.node._cf.fontStyle.node;var font_family='serif';var font_style='normal';var font_justify='left';var font_size=1.0;var font_spacing=1.0;var font_horizontal=true;var font_language="";if(fontStyleNode!==null)
{var fonts=fontStyleNode._vf.family.toString();fonts=fonts.trim().replace(/\'/g,'').replace(/\,/,' ');fonts=fonts.split(" ");font_family=Array.map(fonts,function(s){if(s=='SANS'){return'sans-serif';}
else if(s=='SERIF'){return'serif';}
else if(s=='TYPEWRITER'){return'monospace';}
else{return''+s+'';}}).join(",");font_style=fontStyleNode._vf.style.toString().replace(/\'/g,'');switch(font_style.toUpperCase()){case'PLAIN':font_style='normal';break;case'BOLD':font_style='bold';break;case'ITALIC':font_style='italic';break;case'BOLDITALIC':font_style='italic bold';break;default:font_style='normal';}
var leftToRight=fontStyleNode._vf.leftToRight?'ltr':'rtl';var topToBottom=fontStyleNode._vf.topToBottom;font_justify=fontStyleNode._vf.justify[0].toString().replace(/\'/g,'');switch(font_justify.toUpperCase()){case'BEGIN':font_justify='left';break;case'END':font_justify='right';break;case'FIRST':font_justify='left';break;case'MIDDLE':font_justify='center';break;default:font_justify='left';break;}
font_size=fontStyleNode._vf.size;font_spacing=fontStyleNode._vf.spacing;font_horizontal=fontStyleNode._vf.horizontal;font_language=fontStyleNode._vf.language;if(font_size<0.1)font_size=0.1;if(font_size>2.3)font_size=2.3;}
var textX,textY;var paragraph=this.node._vf.string;var text_canvas=document.createElement('canvas');text_canvas.dir=leftToRight;var textHeight=font_size*42;var textAlignment=font_justify;document.body.appendChild(text_canvas);var text_ctx=text_canvas.getContext('2d');text_ctx.font=font_style+" "+textHeight+"px "+font_family;var maxWidth=text_ctx.measureText(paragraph[0]).width;for(var i=1;i<paragraph.length;i++){if(text_ctx.measureText(paragraph[i]).width>maxWidth)
maxWidth=text_ctx.measureText(paragraph[i]).width;}
text_canvas.width=maxWidth;text_canvas.height=textHeight*paragraph.length;switch(textAlignment){case"left":textX=0;break;case"center":textX=text_canvas.width/2;break;case"right":textX=text_canvas.width;break;}
var txtW=text_canvas.width;var txtH=text_canvas.height;text_ctx.fillStyle='rgba(0,0,0,0)';text_ctx.fillRect(0,0,text_ctx.canvas.width,text_ctx.canvas.height);text_ctx.fillStyle='white';text_ctx.lineWidth=2.5;text_ctx.strokeStyle='grey';text_ctx.textBaseline='top';text_ctx.font=font_style+" "+textHeight+"px "+font_family;text_ctx.textAlign=textAlignment;for(var i=0;i<paragraph.length;i++){textY=i*textHeight;text_ctx.fillText(paragraph[i],textX,textY);}
if(this.texture===null)
{this.texture=gl.createTexture();}
gl.bindTexture(this.type,this.texture);gl.texImage2D(this.type,0,this.format,this.format,gl.UNSIGNED_BYTE,text_canvas);gl.bindTexture(this.type,null);document.body.removeChild(text_canvas);var w=txtW/100.0;var h=txtH/100.0;this.node._mesh._positions[0]=[-w,-h+.4,0,w,-h+.4,0,w,h+.4,0,-w,h+.4,0];};x3dom.shader={};x3dom.shader.PICKING="picking";x3dom.shader.PICKING_24="picking24";x3dom.shader.PICKING_COLOR="pickingColor";x3dom.shader.PICKING_TEXCOORD="pickingTexCoord";x3dom.shader.FRONTGROUND_TEXTURE="frontgroundTexture";x3dom.shader.BACKGROUND_TEXTURE="backgroundTexture";x3dom.shader.BACKGROUND_SKYTEXTURE="backgroundSkyTexture";x3dom.shader.BACKGROUND_CUBETEXTURE="backgroundCubeTexture";x3dom.shader.SHADOW="shadow";x3dom.shader.DEPTH="depth";x3dom.shader.NORMAL="normal";x3dom.shader.material=function(){var shaderPart="uniform vec3  diffuseColor;\n"+"uniform vec3  specularColor;\n"+"uniform vec3  emissiveColor;\n"+"uniform float shininess;\n"+"uniform float transparency;\n"+"uniform float ambientIntensity;\n";return shaderPart;};x3dom.shader.fog=function(){var shaderPart="uniform vec3  fogColor;\n"+"uniform float fogType;\n"+"uniform float fogRange;\n"+"varying vec3 fragEyePosition;\n"+"float calcFog(in vec3 eye) {\n"+"   float f0 = 0.0;\n"+"   if(fogType == 0.0) {\n"+"       if(length(eye) < fogRange){\n"+"           f0 = (fogRange-length(eye)) / fogRange;\n"+"       }\n"+"   }else{\n"+"       if(length(eye) < fogRange){\n"+"           f0 = exp(-length(eye) / (fogRange-length(eye) ) );\n"+"       }\n"+"   }\n"+"   f0 = clamp(f0, 0.0, 1.0);\n"+"   return f0;\n"+"}\n";return shaderPart;};x3dom.shader.shadow=function(){var shaderPart="uniform sampler2D sh_tex;\n"+"varying vec4 projCoord;\n"+"float PCF_Filter(float lShadowIntensity, vec3 projectiveBiased, float filterWidth)\n"+"{\n"+"    float stepSize = 2.0 * filterWidth / 3.0;\n"+"    float blockerCount = 0.0;\n"+"    projectiveBiased.x -= filterWidth;\n"+"    projectiveBiased.y -= filterWidth;\n"+"    for (float i=0.0; i<3.0; i++)\n"+"    {\n"+"        for (float j=0.0; j<3.0; j++)\n"+"        {\n"+"            projectiveBiased.x += (j*stepSize);\n"+"            projectiveBiased.y += (i*stepSize);\n"+"            vec4 zCol = texture2D(sh_tex, (1.0+projectiveBiased.xy)*0.5);\n";if(!x3dom.caps.FP_TEXTURES){shaderPart+="            float fromFixed = 256.0 / 255.0;\n"+"            float z = zCol.r * fromFixed;\n"+"            z += zCol.g * fromFixed / (255.0);\n"+"            z += zCol.b * fromFixed / (255.0 * 255.0);\n"+"            z += zCol.a * fromFixed / (255.0 * 255.0 * 255.0);\n";}
else{shaderPart+="            float z = zCol.b;\n";}
shaderPart+="            if (z < projectiveBiased.z) blockerCount += 1.0;\n"+"            projectiveBiased.x -= (j*stepSize);\n"+"            projectiveBiased.y -= (i*stepSize);\n"+"        }\n"+"    }"+"    float result = 1.0 - lShadowIntensity * blockerCount / 9.0;\n"+"    return result;\n"+"}\n";return shaderPart;};x3dom.shader.light=function(numLights){var shaderPart="";for(var l=0;l<numLights;l++){shaderPart+="uniform float light"+l+"_On;\n"+"uniform float light"+l+"_Type;\n"+"uniform vec3  light"+l+"_Location;\n"+"uniform vec3  light"+l+"_Direction;\n"+"uniform vec3  light"+l+"_Color;\n"+"uniform vec3  light"+l+"_Attenuation;\n"+"uniform float light"+l+"_Radius;\n"+"uniform float light"+l+"_Intensity;\n"+"uniform float light"+l+"_AmbientIntensity;\n"+"uniform float light"+l+"_BeamWidth;\n"+"uniform float light"+l+"_CutOffAngle;\n"+"uniform float light"+l+"_ShadowIntensity;\n";}
shaderPart+="void lighting(in float lType, in vec3 lLocation, in vec3 lDirection, in vec3 lColor, in vec3 lAttenuation, "+"in float lRadius, in float lIntensity, in float lAmbientIntensity, in float lBeamWidth, "+"in float lCutOffAngle, in vec3 N, in vec3 V, inout vec3 ambient, inout vec3 diffuse, "+"inout vec3 specular)\n"+"{\n"+"   vec3 L;\n"+"   float spot = 1.0, attentuation = 0.0;\n"+"   if(lType == 0.0) {\n"+"       L = -normalize(lDirection);\n"+"  V = normalize(V);\n"+"  attentuation = 1.0;\n"+"   } else{\n"+"       L = (lLocation - (-V));\n"+"       float d = length(L);\n"+"  L = normalize(L);\n"+"  V = normalize(V);\n"+"       if(lRadius == 0.0 || d <= lRadius) {\n"+"        attentuation = 1.0 / max(lAttenuation.x + lAttenuation.y * d + lAttenuation.z * (d * d), 1.0);\n"+"  }\n"+"       if(lType == 2.0) {\n"+"           float spotAngle = acos(max(0.0, dot(-L, normalize(lDirection))));\n"+"           if(spotAngle >= lCutOffAngle) spot = 0.0;\n"+"           else if(spotAngle <= lBeamWidth) spot = 1.0;\n"+"           else spot = (spotAngle - lCutOffAngle ) / (lBeamWidth - lCutOffAngle);\n"+"       }\n"+"   }\n"+"   vec3  H = normalize( L + V );\n"+"   float NdotL = max(0.0, dot(L, N));\n"+"   float NdotH = max(0.0, dot(H, N));\n"+"   float ambientFactor  = lAmbientIntensity * ambientIntensity;\n"+"   float diffuseFactor  = lIntensity * NdotL;\n"+"   float specularFactor = lIntensity * pow(NdotH, shininess*128.0);\n"+"   ambient  += lColor * ambientFactor * attentuation * spot;\n"+"   diffuse  += lColor * diffuseFactor * attentuation * spot;\n"+"   specular += lColor * specularFactor * attentuation * spot;\n"+"}\n";return shaderPart;};x3dom.shader.DynamicShader=function(gl,properties)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl,properties);var fragmentShader=this.generateFragmentShader(gl,properties);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.DynamicShader.prototype.generateVertexShader=function(gl,properties)
{var shader="";shader+="uniform mat4 modelViewMatrix;\n";shader+="uniform mat4 modelViewProjectionMatrix;\n";if(properties.POSCOMPONENTS==3){shader+="attribute vec3 position;\n";}else if(properties.POSCOMPONENTS==4){shader+="attribute vec4 position;\n";}
if(properties.IMAGEGEOMETRY){shader+="uniform vec3 IG_bboxMin;\n";shader+="uniform vec3 IG_bboxMax;\n";shader+="uniform float IG_coordTextureWidth;\n";shader+="uniform float IG_coordTextureHeight;\n";shader+="uniform vec2 IG_implicitMeshSize;\n";for(var i=0;i<properties.IG_PRECISION;i++){shader+="uniform sampler2D IG_coords"+i+"\n;";}
if(properties.IG_INDEXED){shader+="uniform sampler2D IG_index;\n";shader+="uniform float IG_indexTextureWidth;\n";shader+="uniform float IG_indexTextureHeight;\n";}}
if(properties.POPGEOMETRY){shader+="uniform float PG_precisionLevel;\n";shader+="uniform float PG_powPrecision;\n";shader+="uniform vec3 PG_bbMin;\n";shader+="uniform vec3 PG_bbMaxModF;\n";shader+="uniform vec3 PG_bboxShiftVec;\n";shader+="uniform float PG_numAnchorVertices;\n";shader+="attribute float PG_vertexID;\n";}
if(properties.LIGHTS){shader+="varying vec3 fragNormal;\n";shader+="uniform mat4 normalMatrix;\n";if(properties.IMAGEGEOMETRY){shader+="uniform sampler2D IG_normals;\n";}else{if(properties.NORCOMPONENTS==2){if(properties.POSCOMPONENTS!=4){shader+="attribute vec2 normal;\n";}}else if(properties.NORCOMPONENTS==3){shader+="attribute vec3 normal;\n";}}}
if(properties.VERTEXCOLOR){if(properties.IMAGEGEOMETRY){shader+="uniform sampler2D IG_colors;\n";if(properties.COLCOMPONENTS==3){shader+="varying vec3 fragColor;\n";}else if(properties.COLCOMPONENTS==4){shader+="varying vec4 fragColor;\n";}}else{if(properties.COLCOMPONENTS==3){shader+="attribute vec3 color;\n";shader+="varying vec3 fragColor;\n";}else if(properties.COLCOMPONENTS==4){shader+="attribute vec4 color;\n";shader+="varying vec4 fragColor;\n";}}}
if(properties.TEXTURED||properties.CSSHADER){shader+="varying vec2 fragTexcoord;\n";if(!properties.SPHEREMAPPING){if(properties.IMAGEGEOMETRY){shader+="uniform sampler2D IG_texCoords;\n";}else{shader+="attribute vec2 texcoord;\n";}}
if(properties.TEXTRAFO){shader+="uniform mat4 texTrafoMatrix;\n";}
if(properties.NORMALMAP){shader+="attribute vec3 tangent;\n";shader+="attribute vec3 binormal;\n";shader+="varying vec3 fragTangent;\n";shader+="varying vec3 fragBinormal;\n";}
if(properties.CUBEMAP){shader+="varying vec3 fragViewDir;\n";shader+="uniform mat4 viewMatrix;\n";}}
if(properties.LIGHTS||properties.FOG){shader+="uniform vec3 eyePosition;\n";shader+="varying vec3 fragPosition;\n";if(properties.FOG){shader+="varying vec3 fragEyePosition;\n";}
if(properties.SHADOW){shader+="uniform mat4 matPV;\n";shader+="varying vec4 projCoord;\n";}}
if(properties.REQUIREBBOX){shader+="uniform vec3 bgCenter;\n";shader+="uniform vec3 bgSize;\n";shader+="uniform float bgPrecisionMax;\n";}
if(properties.REQUIREBBOXNOR){shader+="uniform float bgPrecisionNorMax;\n";}
if(properties.REQUIREBBOXCOL){shader+="uniform float bgPrecisionColMax;\n";}
if(properties.REQUIREBBOXTEX){shader+="uniform float bgPrecisionTexMax;\n";}
shader+="void main(void) {\n";shader+="gl_PointSize = 2.0;\n";if(properties.IMAGEGEOMETRY){if(properties.IG_INDEXED){shader+="vec2 halfPixel = vec2(0.5/IG_indexTextureWidth,0.5/IG_indexTextureHeight);\n";shader+="vec2 IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_indexTextureWidth), position.y*(IG_implicitMeshSize.y/IG_indexTextureHeight)) + halfPixel;\n";shader+="vec2 IG_indices = texture2D( IG_index, IG_texCoord ).rg;\n";shader+="halfPixel = vec2(0.5/IG_coordTextureWidth,0.5/IG_coordTextureHeight);\n";shader+="IG_texCoord = (IG_indices * 0.996108948) + halfPixel;\n";}else{shader+="vec2 halfPixel = vec2(0.5/IG_coordTextureWidth, 0.5/IG_coordTextureHeight);\n";shader+="vec2 IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_coordTextureWidth), position.y*(IG_implicitMeshSize.y/IG_coordTextureHeight)) + halfPixel;\n";}
shader+="vec3 temp = vec3(0.0, 0.0, 0.0);\n";shader+="vec3 vertPosition = vec3(0.0, 0.0, 0.0);\n";for(var i=0;i<properties.IG_PRECISION;i++){shader+="temp = 255.0 * texture2D( IG_coords"+i+", IG_texCoord ).rgb;\n";shader+="vertPosition *= 256.0;\n";shader+="vertPosition += temp;\n";}
shader+="vertPosition /= (pow(2.0, 8.0 * "+properties.IG_PRECISION+".0) - 1.0);\n";shader+="vertPosition = vertPosition * (IG_bboxMax - IG_bboxMin) + IG_bboxMin;\n";if(properties.LIGHTS){shader+="vec3 vertNormal = texture2D( IG_normals, IG_texCoord ).rgb;\n";shader+="vertNormal = vertNormal * 2.0 - 1.0;\n";}
if(properties.VERTEXCOLOR){if(properties.COLCOMPONENTS==3){shader+="fragColor = texture2D( IG_colors, IG_texCoord ).rgb;\n";}else if(properties.COLCOMPONENTS==4){shader+="fragColor = texture2D( IG_colors, IG_texCoord ).rgba;\n";}}
if(properties.TEXTURED||properties.CSSHADER){shader+="vec4 IG_doubleTexCoords = texture2D( IG_texCoords, IG_texCoord );\n";shader+="vec2 vertTexCoord;";shader+="vertTexCoord.r = (IG_doubleTexCoords.r * 0.996108948) + (IG_doubleTexCoords.b * 0.003891051);\n";shader+="vertTexCoord.g = (IG_doubleTexCoords.g * 0.996108948) + (IG_doubleTexCoords.a * 0.003891051);\n";}}else{shader+="vec3 vertPosition = position.xyz;\n";if(properties.POPGEOMETRY){shader+="vec3 offsetVec = step(vertPosition / bgPrecisionMax, PG_bbMaxModF) * PG_bboxShiftVec;\n";shader+="if ((PG_precisionLevel <= 2.0) || PG_vertexID >= PG_numAnchorVertices) {\n";shader+="   vertPosition = floor(vertPosition / PG_powPrecision) * PG_powPrecision;\n";shader+="   vertPosition /= (65536.0 - PG_powPrecision);\n";shader+="}\n";shader+="else {\n";shader+="   vertPosition /= bgPrecisionMax;\n";shader+="}\n";shader+="vertPosition = (vertPosition + offsetVec + PG_bbMin) * bgSize;\n";}
else if(properties.REQUIREBBOX){shader+="vertPosition = bgCenter + bgSize * vertPosition / bgPrecisionMax;\n";}
if(properties.LIGHTS){if(properties.NORCOMPONENTS==2){if(properties.POSCOMPONENTS==4){shader+="vec3 vertNormal = vec3(position.w / 256.0); \n";shader+="vertNormal.x = floor(vertNormal.x) / 255.0; \n";shader+="vertNormal.y = fract(vertNormal.y) * 1.00392156862745; \n";}
else if(properties.REQUIREBBOXNOR){shader+="vec3 vertNormal = vec3(normal.xy, 0.0) / bgPrecisionNorMax;\n";}
shader+="vec2 thetaPhi = 3.14159265358979 * vec2(vertNormal.x, vertNormal.y*2.0-1.0); \n";shader+="vec4 sinCosThetaPhi = sin( vec4(thetaPhi, thetaPhi + 1.5707963267949) ); \n";shader+="vertNormal.x = sinCosThetaPhi.x * sinCosThetaPhi.w; \n";shader+="vertNormal.y = sinCosThetaPhi.x * sinCosThetaPhi.y; \n";shader+="vertNormal.z = sinCosThetaPhi.z; \n";}else{shader+="vec3 vertNormal = normal;\n";if(properties.REQUIREBBOXNOR){shader+="vertNormal = vertNormal / bgPrecisionNorMax;\n";}
if(properties.BITLODGEOMETRY||properties.POPGEOMETRY){shader+="vertNormal = 2.0*vertNormal - 1.0;\n";}}}
if(properties.VERTEXCOLOR){shader+="fragColor = color;\n";if(properties.REQUIREBBOXCOL){shader+="fragColor = fragColor / bgPrecisionColMax;\n";}}
if((properties.TEXTURED||properties.CSSHADER)&&!properties.SPHEREMAPPING){shader+="vec2 vertTexCoord = texcoord;\n";if(properties.REQUIREBBOXTEX){shader+="vertTexCoord = vertTexCoord / bgPrecisionTexMax;\n";}}}
if(properties.LIGHTS){shader+="fragNormal = (normalMatrix * vec4(vertNormal, 0.0)).xyz;\n";}
if(properties.TEXTURED||properties.CSSHADER){if(properties.CUBEMAP){shader+="fragViewDir = (viewMatrix[3].xyz);\n";}else if(properties.SPHEREMAPPING){shader+=" fragTexcoord = 0.5 + fragNormal.xy / 2.0;\n";}else if(properties.TEXTRAFO){shader+=" fragTexcoord = (texTrafoMatrix * vec4(vertTexCoord, 1.0, 1.0)).xy;\n";}else{shader+=" fragTexcoord = vertTexCoord;\n";if(properties.POPGEOMETRY&&x3dom.debug.usePrecisionLevelAsTexCoord===true)
shader+="fragTexcoord = vec2(0.03125 + 0.9375 * (PG_precisionLevel / 16.0), 1.0);";}
if(properties.NORMALMAP){shader+="fragTangent  = (normalMatrix * vec4(tangent, 0.0)).xyz;\n";shader+="fragBinormal = (normalMatrix * vec4(binormal, 0.0)).xyz;\n";}}
if(properties.LIGHTS||properties.FOG){shader+="fragPosition = (modelViewMatrix * vec4(vertPosition, 1.0)).xyz;\n";if(properties.FOG){shader+="fragEyePosition = eyePosition - fragPosition;\n";}
if(properties.SHADOW){shader+="projCoord = matPV * vec4(vertPosition+0.5*normalize(vertNormal), 1.0);\n";}}
shader+="gl_Position = modelViewProjectionMatrix * vec4(vertPosition, 1.0);\n";shader+="}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.DynamicShader.prototype.generateFragmentShader=function(gl,properties)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n\n";shader+="uniform mat4 modelMatrix;\n";shader+="uniform mat4 modelViewMatrix;\n";shader+=x3dom.shader.material();if(properties.VERTEXCOLOR){if(properties.COLCOMPONENTS==3){shader+="varying vec3 fragColor;  \n";}else if(properties.COLCOMPONENTS==4){shader+="varying vec4 fragColor;  \n";}}
if(properties.TEXTURED||properties.CSSHADER){shader+="varying vec2 fragTexcoord;\n";if((properties.TEXTURED||properties.DIFFUSEMAP)&&!properties.CUBEMAP){shader+="uniform sampler2D diffuseMap;\n";}else if(properties.CUBEMAP){shader+="uniform samplerCube cubeMap;\n";shader+="varying vec3 fragViewDir;\n";shader+="uniform mat4 modelViewMatrixInverse;\n";}
if(properties.NORMALMAP){shader+="uniform sampler2D normalMap;\n";shader+="varying vec3 fragTangent;\n";shader+="varying vec3 fragBinormal;\n";}
if(properties.SPECMAP){shader+="uniform sampler2D specularMap;\n";}}
if(properties.FOG){shader+=x3dom.shader.fog();}
if(properties.LIGHTS){shader+="varying vec3 fragNormal;\n";shader+="varying vec3 fragPosition;\n";shader+=x3dom.shader.light(properties.LIGHTS);if(properties.SHADOW){shader+=x3dom.shader.shadow();}}
shader+="void main(void) {\n";shader+="vec4 color;\n";shader+="color.rgb = diffuseColor;\n";shader+="color.a = 1.0 - transparency;\n";if(properties.VERTEXCOLOR){if(properties.COLCOMPONENTS==3){shader+="color.rgb = fragColor;\n";}else if(properties.COLCOMPONENTS==4){shader+="color = fragColor;\n";}}
if(properties.LIGHTS){shader+="vec3 ambient   = vec3(0.07, 0.07, 0.07);\n";shader+="vec3 diffuse   = vec3(0.0, 0.0, 0.0);\n";shader+="vec3 specular  = vec3(0.0, 0.0, 0.0);\n";shader+="vec3 normal    = normalize(fragNormal);\n";shader+="vec3 eye    = -fragPosition;\n";if(properties.SHADOW){shader+="float shadowed = 1.0;\n";shader+="float oneShadowAlreadyExists = 0.0;\n";}
if(properties.NORMALMAP){shader+="vec3 t = normalize( fragTangent );\n";shader+="vec3 b = normalize( fragBinormal );\n";shader+="vec3 n = normalize( fragNormal );\n";shader+="mat3 tangentToWorld = mat3(t, b, n);\n";shader+="normal = texture2D( normalMap, vec2(fragTexcoord.x, 1.0-fragTexcoord.y) ).rgb;\n";shader+="normal = 2.0 * normal - 1.0;\n";shader+="normal = normalize( normal * tangentToWorld );\n";shader+="normal.y = -normal.y;\n";shader+="normal.x = -normal.x;\n";}
if(!properties.SOLID){shader+="if (dot(normal, eye) < 0.0) {\n";shader+="  normal *= -1.0;\n";shader+="}\n";}
for(var l=0;l<properties.LIGHTS;l++){shader+=" lighting(light"+l+"_Type, "+"light"+l+"_Location, "+"light"+l+"_Direction, "+"light"+l+"_Color, "+"light"+l+"_Attenuation, "+"light"+l+"_Radius, "+"light"+l+"_Intensity, "+"light"+l+"_AmbientIntensity, "+"light"+l+"_BeamWidth, "+"light"+l+"_CutOffAngle, "+"normal, eye, ambient, diffuse, specular);\n";if(properties.SHADOW){shader+=" if(light"+l+"_ShadowIntensity > 0.0 && oneShadowAlreadyExists == 0.0){\n";shader+="     vec3 projectiveBiased = projCoord.xyz / projCoord.w;\n";shader+="     shadowed = PCF_Filter(light"+l+"_ShadowIntensity, projectiveBiased, 0.002);\n";shader+="     oneShadowAlreadyExists = 1.0;\n";shader+=" }\n";}}
if(properties.SPECMAP){shader+="specular *= texture2D(specularMap, vec2(fragTexcoord.x, 1.0-fragTexcoord.y)).rgb;\n";}
if(properties.TEXTURED||properties.DIFFUSEMAP){if(properties.CUBEMAP){shader+="vec3 viewDir = normalize(fragViewDir);\n";shader+="vec3 reflected = reflect(viewDir, normal);\n";shader+="reflected = (modelViewMatrixInverse * vec4(reflected,0.0)).xyz;\n";shader+="vec4 texColor = textureCube(cubeMap, reflected);\n";shader+="color.a *= texColor.a;\n";}else{shader+="vec2 texCoord = vec2(fragTexcoord.x, 1.0-fragTexcoord.y);\n";shader+="vec4 texColor = texture2D(diffuseMap, texCoord);\n";shader+="color.a *= texColor.a;\n";}
if(properties.BLENDING){shader+="color.rgb = (emissiveColor + ambient*color.rgb + diffuse*color.rgb + specular*specularColor);\n";if(properties.CUBEMAP){shader+="color.rgb = mix(color.rgb, texColor.rgb, vec3(0.75));\n";}else{shader+="color.rgb *= texColor.rgb;\n";}}else{shader+="color.rgb = (emissiveColor + ambient*texColor.rgb + diffuse*texColor.rgb + specular*specularColor);\n";}}else{shader+="color.rgb = (emissiveColor + ambient*color.rgb + diffuse*color.rgb + specular*specularColor);\n";}
if(properties.SHADOW){shader+="color.rgb *= shadowed;\n";}}else{if(properties.TEXTURED||properties.DIFFUSEMAP){shader+="vec2 texCoord = vec2(fragTexcoord.x, 1.0-fragTexcoord.y);\n";shader+="vec4 texColor = texture2D(diffuseMap, texCoord);\n";shader+="color.a = texColor.a;\n";if(properties.BLENDING){shader+="color.rgb += emissiveColor.rgb;\n";shader+="color.rgb *= texColor.rgb;\n";}else{shader+="color = texColor;\n";}}else if(!properties.VERTEXCOLOR&&!properties.POINTLINE2D){shader+="color.rgb += emissiveColor;\n";}else if(!properties.VERTEXCOLOR&&properties.POINTLINE2D){shader+="color.rgb = emissiveColor;\n";}}
if(properties.FOG){shader+="float f0 = calcFog(fragEyePosition);\n";shader+="color.rgb = fogColor * (1.0-f0) + f0 * (color.rgb);\n";}
if(properties.TEXT){shader+="if (color.a <= 0.5) discard;\n";}else{shader+="if (color.a <= 0.1) discard;\n";}
shader+="gl_FragColor = color;\n";shader+="}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.DynamicMobileShader=function(gl,properties)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl,properties);var fragmentShader=this.generateFragmentShader(gl,properties);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.DynamicMobileShader.prototype.generateVertexShader=function(gl,properties)
{var shader="";shader+=x3dom.shader.material();shader+="uniform mat4 normalMatrix;\n";shader+="uniform mat4 modelViewMatrix;\n";shader+="uniform mat4 modelViewProjectionMatrix;\n";if(properties.POSCOMPONENTS==3){shader+="attribute vec3 position;\n";}else if(properties.POSCOMPONENTS==4){shader+="attribute vec4 position;\n";}
if(properties.IMAGEGEOMETRY){shader+="uniform vec3 IG_bboxMin;\n";shader+="uniform vec3 IG_bboxMax;\n";shader+="uniform float IG_coordTextureWidth;\n";shader+="uniform float IG_coordTextureHeight;\n";shader+="uniform vec2 IG_implicitMeshSize;\n";for(var i=0;i<properties.IG_PRECISION;i++){shader+="uniform sampler2D IG_coords"+i+"\n;";}
if(properties.IG_INDEXED){shader+="uniform sampler2D IG_index;\n";shader+="uniform float IG_indexTextureWidth;\n";shader+="uniform float IG_indexTextureHeight;\n";}}
if(properties.POPGEOMETRY){shader+="uniform float PG_precisionLevel;\n";shader+="uniform float PG_powPrecision;\n";shader+="uniform vec3 PG_bbMin;\n";shader+="uniform vec3 PG_bbMaxModF;\n";shader+="uniform vec3 PG_bboxShiftVec;\n";shader+="uniform float PG_numAnchorVertices;\n";shader+="attribute float PG_vertexID;\n";}
if(!properties.POINTLINE2D){if(properties.IMAGEGEOMETRY){shader+="uniform sampler2D IG_normals;\n";}else{if(properties.NORCOMPONENTS==2){if(properties.POSCOMPONENTS!=4){shader+="attribute vec2 normal;\n";}}else if(properties.NORCOMPONENTS==3){shader+="attribute vec3 normal;\n";}}}
shader+="varying vec4 fragColor;\n";if(properties.VERTEXCOLOR){if(properties.IMAGEGEOMETRY){shader+="uniform sampler2D IG_colors;";}else{if(properties.COLCOMPONENTS==3){shader+="attribute vec3 color;";}else if(properties.COLCOMPONENTS==4){shader+="attribute vec4 color;";}}}
if(properties.TEXTURED){shader+="varying vec2 fragTexcoord;\n";if(properties.IMAGEGEOMETRY){shader+="uniform sampler2D IG_texCoords;";}else{shader+="attribute vec2 texcoord;\n";}
if(properties.TEXTRAFO){shader+="uniform mat4 texTrafoMatrix;\n";}
if(!properties.BLENDING){shader+="varying vec3 fragAmbient;\n";shader+="varying vec3 fragDiffuse;\n";}
if(properties.CUBEMAP){shader+="varying vec3 fragViewDir;\n";shader+="varying vec3 fragNormal;\n";shader+="uniform mat4 viewMatrix;\n";}}
if(properties.FOG){shader+=x3dom.shader.fog();}
if(properties.LIGHTS){shader+=x3dom.shader.light(properties.LIGHTS);}
if(properties.REQUIREBBOX){shader+="uniform vec3 bgCenter;\n";shader+="uniform vec3 bgSize;\n";shader+="uniform float bgPrecisionMax;\n";}
if(properties.REQUIREBBOXNOR){shader+="uniform float bgPrecisionNorMax;\n";}
if(properties.REQUIREBBOXCOL){shader+="uniform float bgPrecisionColMax;\n";}
if(properties.REQUIREBBOXTEX){shader+="uniform float bgPrecisionTexMax;\n";}
shader+="void main(void) {\n";shader+="gl_PointSize = 2.0;\n";if(properties.IMAGEGEOMETRY){if(properties.IG_INDEXED){shader+="vec2 halfPixel = vec2(0.5/IG_indexTextureWidth,0.5/IG_indexTextureHeight);\n";shader+="vec2 IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_indexTextureWidth), position.y*(IG_implicitMeshSize.y/IG_indexTextureHeight)) + halfPixel;\n";shader+="vec2 IG_indices = texture2D( IG_index, IG_texCoord ).rg;\n";shader+="halfPixel = vec2(0.5/IG_coordTextureWidth,0.5/IG_coordTextureHeight);\n";shader+="IG_texCoord = (IG_indices * 0.996108948) + halfPixel;\n";}else{shader+="vec2 halfPixel = vec2(0.5/IG_coordTextureWidth, 0.5/IG_coordTextureHeight);\n";shader+="vec2 IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_coordTextureWidth), position.y*(IG_implicitMeshSize.y/IG_coordTextureHeight)) + halfPixel;\n";}
shader+="vec3 temp = vec3(0.0, 0.0, 0.0);\n";shader+="vec3 vertPosition = vec3(0.0, 0.0, 0.0);\n";for(var i=0;i<properties.IG_PRECISION;i++){shader+="temp = 255.0 * texture2D( IG_coords"+i+", IG_texCoord ).rgb;\n";shader+="vertPosition *= 256.0;\n";shader+="vertPosition += temp;\n";}
shader+="vertPosition /= (pow(2.0, 8.0 * "+properties.IG_PRECISION+".0) - 1.0);\n";shader+="vertPosition = vertPosition * (IG_bboxMax - IG_bboxMin) + IG_bboxMin;\n";if(!properties.POINTLINE2D){shader+="vec3 vertNormal = texture2D( IG_normals, IG_texCoord ).rgb;\n";shader+="vertNormal = vertNormal * 2.0 - 1.0;\n";}
if(properties.VERTEXCOLOR){if(properties.COLCOMPONENTS==3){shader+="vec3 vertColor = texture2D( IG_colors, IG_texCoord ).rgb;";}else if(properties.COLCOMPONENTS==4){shader+="vec4 vertColor = texture2D( IG_colors, IG_texCoord ).rgba;";}}
if(properties.TEXTURED){shader+="vec4 IG_doubleTexCoords = texture2D( IG_texCoords, IG_texCoord );\n";shader+="vec2 vertTexCoord;";shader+="vertTexCoord.r = (IG_doubleTexCoords.r * 0.996108948) + (IG_doubleTexCoords.b * 0.003891051);\n";shader+="vertTexCoord.g = (IG_doubleTexCoords.g * 0.996108948) + (IG_doubleTexCoords.a * 0.003891051);\n";}}else{shader+="vec3 vertPosition = position.xyz;\n";if(properties.POPGEOMETRY){shader+="vec3 offsetVec = step(vertPosition / bgPrecisionMax, PG_bbMaxModF) * PG_bboxShiftVec;\n";shader+="if ((PG_precisionLevel <= 2.0) || PG_vertexID >= PG_numAnchorVertices) {\n";shader+="   vertPosition = floor(vertPosition / PG_powPrecision) * PG_powPrecision;\n";shader+="   vertPosition /= (65536.0 - PG_powPrecision);\n";shader+="}\n";shader+="else {\n";shader+="   vertPosition /= bgPrecisionMax;\n";shader+="}\n";shader+="vertPosition = (vertPosition + offsetVec + PG_bbMin) * bgSize;\n";}
else if(properties.REQUIREBBOX||properties.BITLODGEOMETRY){shader+="vertPosition = bgCenter + bgSize * vertPosition / bgPrecisionMax;\n";}
if(!properties.POINTLINE2D){if(properties.NORCOMPONENTS==2){if(properties.POSCOMPONENTS==4){shader+="vec3 vertNormal = vec3(position.w / 256.0); \n";shader+="vertNormal.x = floor(vertNormal.x) / 255.0; \n";shader+="vertNormal.y = fract(vertNormal.y) * 1.00392156862745; \n";}else if(properties.REQUIREBBOXNOR&&!properties.BITLODGEOMETRY){shader+="vec3 vertNormal = vec3(normal.xy, 0.0) / bgPrecisionNorMax;\n";}else{shader+="vec3 vertNormal = vec3(normal.xy, 0.0);\n";}
shader+="vec2 thetaPhi = 3.14159265358979 * vec2(vertNormal.x, vertNormal.y*2.0-1.0); \n";shader+="vec4 sinCosThetaPhi = vec4(thetaPhi, thetaPhi + 1.5707963267949); \n";shader+="vec4 thetaPhiPow2 = sinCosThetaPhi * sinCosThetaPhi; \n";shader+="vec4 thetaPhiPow3 =  thetaPhiPow2  * sinCosThetaPhi; \n";shader+="vec4 thetaPhiPow5 =  thetaPhiPow3  * thetaPhiPow2; \n";shader+="vec4 thetaPhiPow7 =  thetaPhiPow5  * thetaPhiPow2; \n";shader+="vec4 thetaPhiPow9 =  thetaPhiPow7  * thetaPhiPow2; \n";shader+="sinCosThetaPhi +=  -0.16666666667   * thetaPhiPow3; \n";shader+="sinCosThetaPhi +=   0.00833333333   * thetaPhiPow5; \n";shader+="sinCosThetaPhi +=  -0.000198412698  * thetaPhiPow7; \n";shader+="sinCosThetaPhi +=   0.0000027557319 * thetaPhiPow9; \n";shader+="vertNormal.x = sinCosThetaPhi.x * sinCosThetaPhi.w; \n";shader+="vertNormal.y = sinCosThetaPhi.x * sinCosThetaPhi.y; \n";shader+="vertNormal.z = sinCosThetaPhi.z; \n";}else{shader+="vec3 vertNormal = normal;\n";if(properties.REQUIREBBOXNOR&&!properties.BITLODGEOMETRY){shader+="vertNormal = vertNormal / bgPrecisionNorMax;\n";}
if(properties.BITLODGEOMETRY||properties.POPGEOMETRY){shader+="vertNormal = 2.0*vertNormal - 1.0;\n";}}}
if(properties.VERTEXCOLOR){if(properties.COLCOMPONENTS==3){shader+="vec3 vertColor = color;";}else if(properties.COLCOMPONENTS==4){shader+="vec4 vertColor = color;";}
if(properties.REQUIREBBOXNOR){shader+="vertColor = vertColor / bgPrecisionColMax;\n";}}
if(properties.TEXTURED){shader+="vec2 vertTexCoord = texcoord;\n";if(properties.REQUIREBBOXTEX){shader+="vertTexCoord = vertTexCoord / bgPrecisionTexMax;\n";}}}
shader+="vec3 positionMV = (modelViewMatrix * vec4(vertPosition, 1.0)).xyz;\n";if(!properties.POINTLINE2D){shader+="vec3 normalMV = normalize( (normalMatrix * vec4(vertNormal, 0.0)).xyz );\n";}
shader+="vec3 eye = -positionMV;\n";if(properties.VERTEXCOLOR){shader+="vec3 rgb = vertColor.rgb;\n";if(properties.COLCOMPONENTS==4){shader+="float alpha = vertColor.a;\n";}else if(properties.COLCOMPONENTS==3){shader+="float alpha = 1.0 - transparency;\n";}}else{shader+="vec3 rgb = diffuseColor;\n";shader+="float alpha = 1.0 - transparency;\n";}
if(properties.TEXTURED){if(properties.CUBEMAP){shader+="fragViewDir = viewMatrix[3].xyz;\n";shader+="fragNormal = normalMV;\n";}else if(properties.SPHEREMAPPING){shader+=" fragTexcoord = 0.5 + normalMV.xy / 2.0;\n";}else if(properties.TEXTRAFO){shader+=" fragTexcoord = (texTrafoMatrix * vec4(vertTexCoord, 1.0, 1.0)).xy;\n";}else{shader+=" fragTexcoord = vertTexCoord;\n";if(properties.POPGEOMETRY&&x3dom.debug.usePrecisionLevelAsTexCoord===true)
shader+="fragTexcoord = vec2(0.03125 + 0.9375 * (PG_precisionLevel / 16.0), 1.0);";}}
if(properties.LIGHTS){shader+="vec3 ambient   = vec3(0.07, 0.07, 0.07);\n";shader+="vec3 diffuse   = vec3(0.0, 0.0, 0.0);\n";shader+="vec3 specular  = vec3(0.0, 0.0, 0.0);\n";if(!properties.SOLID){shader+="if (dot(normalMV, eye) < 0.0) {\n";shader+="  normalMV *= -1.0;\n";shader+="}\n";}
for(var i=0;i<properties.LIGHTS;i++){shader+=" lighting(light"+i+"_Type,"+"light"+i+"_Location,"+"light"+i+"_Direction,"+"light"+i+"_Color,"+"light"+i+"_Attenuation,"+"light"+i+"_Radius,"+"light"+i+"_Intensity,"+"light"+i+"_AmbientIntensity,"+"light"+i+"_BeamWidth,"+"light"+i+"_CutOffAngle,"+"normalMV, eye, ambient, diffuse, specular);\n";}
if(properties.TEXTURED&&!properties.BLENDING){shader+="fragAmbient = ambient;\n";shader+="fragDiffuse = diffuse;\n";shader+="fragColor.rgb = (emissiveColor + specular*specularColor);\n";shader+="fragColor.a = alpha;\n";}else{shader+="fragColor.rgb = (emissiveColor + ambient*rgb + diffuse*rgb + specular*specularColor);\n";shader+="fragColor.a = alpha;\n";}}else{if(properties.TEXTURED&&!properties.BLENDING){shader+="fragAmbient = vec3(1.0);\n";shader+="fragDiffuse = vec3(1.0);\n";shader+="fragColor.rgb = vec3(0.0);\n";shader+="fragColor.a = alpha;\n";}else if(!properties.VERTEXCOLOR&&properties.POINTLINE2D){shader+="fragColor.rgb = emissiveColor;\n";shader+="fragColor.a = alpha;\n";}else{shader+="fragColor.rgb = rgb + emissiveColor;\n;\n";shader+="fragColor.a = alpha;\n";}}
if(properties.FOG){shader+="float f0 = calcFog(-positionMV);\n";shader+="fragColor.rgb = fogColor * (1.0-f0) + f0 * (fragColor.rgb);\n";}
shader+="gl_Position = modelViewProjectionMatrix * vec4(vertPosition, 1.0);\n";shader+="}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[DynamicMobileShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.DynamicMobileShader.prototype.generateFragmentShader=function(gl,properties)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n\n";shader+="varying vec4 fragColor;\n";if(properties.TEXTURED){if(properties.CUBEMAP){shader+="uniform samplerCube cubeMap;\n";shader+="varying vec3 fragViewDir;\n";shader+="varying vec3 fragNormal;\n";shader+="uniform mat4 modelViewMatrixInverse;\n";}else{shader+="uniform sampler2D diffuseMap;           \n";shader+="varying vec2 fragTexcoord;       \n";}
if(!properties.BLENDING){shader+="varying vec3 fragAmbient;\n";shader+="varying vec3 fragDiffuse;\n";}}
shader+="void main(void) {\n";shader+="vec4 color = fragColor;\n";if(properties.TEXTURED){if(properties.CUBEMAP){shader+="vec3 normal = normalize(fragNormal);\n";shader+="vec3 viewDir = normalize(fragViewDir);\n";shader+="vec3 reflected = reflect(viewDir, normal);\n";shader+="reflected = (modelViewMatrixInverse * vec4(reflected,0.0)).xyz;\n";shader+="vec4 texColor = textureCube(cubeMap, reflected);\n";}else{shader+="vec4 texColor = texture2D(diffuseMap, vec2(fragTexcoord.s, 1.0-fragTexcoord.t));\n";}
if(properties.BLENDING){if(properties.CUBEMAP){shader+="color.rgb = mix(color.rgb, texColor.rgb, vec3(0.75));\n";shader+="color.a = texColor.a;\n";}else{shader+="color.rgb *= texColor.rgb;\n";shader+="color.a *= texColor.a;\n";}}else{shader+="color.rgb += fragAmbient*texColor.rgb + fragDiffuse*texColor.rgb;\n";shader+="color.a *= texColor.a;\n";}}
if(properties.TEXT){shader+="if (color.a <= 0.5) discard;\n";}else{shader+="if (color.a <= 0.1) discard;\n";}
shader+="gl_FragColor = color;\n";shader+="}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[DynamicMobileShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.ComposedShader=function(gl,shape)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl,shape);var fragmentShader=this.generateFragmentShader(gl,shape);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.ComposedShader.prototype.generateVertexShader=function(gl,shape)
{var shader=shape._cf.appearance.node._shader._vertex._vf.url[0];var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[ComposedShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.ComposedShader.prototype.generateFragmentShader=function(gl,shape)
{var shader=shape._cf.appearance.node._shader._fragment._vf.url[0];var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[ComposedShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.NormalShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.NormalShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"attribute vec3 normal;\n"+"uniform vec3 bgCenter;\n"+"uniform vec3 bgSize;\n"+"uniform float bgPrecisionMax;\n"+"uniform float bgPrecisionNorMax;\n"+"uniform mat4 normalMatrix;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"varying vec3 fragNormal;\n"+"void main(void) {\n"+"    vec3 pos = bgCenter + bgSize * position / bgPrecisionMax;\n"+"    fragNormal = (normalMatrix * vec4(normal / bgPrecisionNorMax, 0.0)).xyz;\n"+"    gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[NormalShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.NormalShader.prototype.generateFragmentShader=function(gl)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"varying vec3 fragNormal;\n"+"void main(void) {\n"+"    gl_FragColor = vec4(normalize(fragNormal) / 2.0 + 0.5, 1.0);\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[NormalShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.PickingShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.PickingShader.prototype.generateVertexShader=function(gl)
{var shader="";if(!x3dom.caps.MOBILE){shader="attribute vec3 position;\n"+"attribute vec2 texcoord;\n"+"uniform vec3 bgCenter;\n"+"uniform vec3 bgSize;\n"+"uniform float bgPrecisionMax;\n"+"uniform mat4 modelMatrix;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"uniform vec3 from;\n"+"varying vec3 worldCoord;\n"+"varying vec2 idCoord;\n"+"uniform float writeShadowIDs;\n"+"uniform float imageGeometry;\n"+"uniform vec3 IG_bboxMin;\n"+"uniform vec3 IG_bboxMax;\n"+"uniform float IG_coordTextureWidth;\n"+"uniform float IG_coordTextureHeight;\n"+"uniform float IG_indexTextureWidth;\n"+"uniform float IG_indexTextureHeight;\n"+"uniform sampler2D IG_indexTexture;\n"+"uniform sampler2D IG_coordinateTexture;\n"+"uniform vec2 IG_implicitMeshSize;\n"+"void main(void) {\n"+"   if (writeShadowIDs > 0.0) {\n"+"     idCoord = vec2((texcoord.x + writeShadowIDs) / 256.0);\n"+"       idCoord.x = floor(idCoord.x) / 255.0;\n"+"       idCoord.y = fract(idCoord.y) * 1.00392156862745;\n"+" }\n"+" if (imageGeometry != 0.0) {\n"+"  vec2 IG_texCoord;\n"+"  if(imageGeometry == 1.0) {\n"+"   vec2 halfPixel = vec2(0.5/IG_indexTextureWidth,0.5/IG_indexTextureHeight);\n"+"   IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_indexTextureWidth), position.y*(IG_implicitMeshSize.y/IG_indexTextureHeight)) + halfPixel;\n"+"   vec2 IG_index = texture2D( IG_indexTexture, IG_texCoord ).rg;\n"+"   IG_texCoord = IG_index * 0.996108948;\n"+"  } else {\n"+"   vec2 halfPixel = vec2(0.5/IG_coordTextureWidth, 0.5/IG_coordTextureHeight);\n"+"   IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_coordTextureWidth), position.y*(IG_implicitMeshSize.y/IG_coordTextureHeight)) + halfPixel;\n"+"  }\n"+"  vec3 pos = texture2D( IG_coordinateTexture, IG_texCoord ).rgb;\n"+"   pos = pos * (IG_bboxMax - IG_bboxMin) + IG_bboxMin;\n"+"     worldCoord = (modelMatrix * vec4(pos, 1.0)).xyz - from;\n"+"  gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+" } else {\n"+"  vec3 pos = bgCenter + bgSize * position / bgPrecisionMax;\n"+"  worldCoord = (modelMatrix * vec4(pos, 1.0)).xyz - from;\n"+"  gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+" }\n"+"}\n";}
else{shader="attribute vec3 position;\n"+"attribute vec2 texcoord;\n"+"uniform vec3 bgCenter;\n"+"uniform vec3 bgSize;\n"+"uniform float bgPrecisionMax;\n"+"uniform float writeShadowIDs;\n"+"uniform mat4 modelMatrix;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"uniform vec3 from;\n"+"varying vec3 worldCoord;\n"+"varying vec2 idCoord;\n"+"void main(void) {\n"+"    if (writeShadowIDs > 0.0) {\n"+"     idCoord = vec2((texcoord.x + writeShadowIDs) / 256.0);\n"+"       idCoord.x = floor(idCoord.x) / 255.0;\n"+"       idCoord.y = fract(idCoord.y) * 1.00392156862745;\n"+"  }\n"+"    vec3 pos = bgCenter + bgSize * position / bgPrecisionMax;\n"+"    worldCoord = (modelMatrix * vec4(pos, 1.0)).xyz - from;\n"+"    gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+"}\n";}
var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[PickingShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.PickingShader.prototype.generateFragmentShader=function(gl)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform float writeShadowIDs;\n"+"uniform float highBit;\n"+"uniform float lowBit;\n"+"uniform float sceneSize;\n"+"varying vec3 worldCoord;\n"+"varying vec2 idCoord;\n"+"void main(void) {\n"+"    vec4 col = vec4(0.0, 0.0, highBit, lowBit);\n"+"    if (writeShadowIDs > 0.0) {\n"+"       col.ba = idCoord;\n"+"  }\n"+"    float d = length(worldCoord) / sceneSize;\n"+"    vec2 comp = fract(d * vec2(256.0, 1.0));\n"+"    col.rg = comp - (comp.rr * vec2(0.0, 1.0/256.0));\n"+"    gl_FragColor = col;\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[PickingShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.Picking24Shader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.Picking24Shader.prototype.generateVertexShader=function(gl)
{var shader="";if(!x3dom.caps.MOBILE){shader="attribute vec3 position;\n"+"attribute vec2 texcoord;\n"+"uniform vec3 bgCenter;\n"+"uniform vec3 bgSize;\n"+"uniform float bgPrecisionMax;\n"+"uniform mat4 modelMatrix;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"uniform vec3 from;\n"+"varying vec3 worldCoord;\n"+"varying vec3 idCoord;\n"+"uniform float writeShadowIDs;\n"+"uniform float imageGeometry;\n"+"uniform vec3 IG_bboxMin;\n"+"uniform vec3 IG_bboxMax;\n"+"uniform float IG_coordTextureWidth;\n"+"uniform float IG_coordTextureHeight;\n"+"uniform float IG_indexTextureWidth;\n"+"uniform float IG_indexTextureHeight;\n"+"uniform sampler2D IG_indexTexture;\n"+"uniform sampler2D IG_coordinateTexture;\n"+"uniform vec2 IG_implicitMeshSize;\n"+"void main(void) {\n"+"   if (writeShadowIDs > 0.0) {\n"+"       float ID = (texcoord.y * 65536.0 + texcoord.x) + writeShadowIDs;\n"+"       float h = floor(ID / 256.0);\n"+"       idCoord.x = ID - (h * 256.0);\n"+"       idCoord.z = floor(h / 256.0);\n"+"       idCoord.y = h - (idCoord.z * 256.0);\n"+"       idCoord = idCoord.zyx / 255.0;\n"+" }\n"+" if (imageGeometry != 0.0) {\n"+"  vec2 IG_texCoord;\n"+"  if(imageGeometry == 1.0) {\n"+"   vec2 halfPixel = vec2(0.5/IG_indexTextureWidth,0.5/IG_indexTextureHeight);\n"+"   IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_indexTextureWidth), position.y*(IG_implicitMeshSize.y/IG_indexTextureHeight)) + halfPixel;\n"+"   vec2 IG_index = texture2D( IG_indexTexture, IG_texCoord ).rg;\n"+"   IG_texCoord = IG_index * 0.996108948;\n"+"  } else {\n"+"   vec2 halfPixel = vec2(0.5/IG_coordTextureWidth, 0.5/IG_coordTextureHeight);\n"+"   IG_texCoord = vec2(position.x*(IG_implicitMeshSize.x/IG_coordTextureWidth), position.y*(IG_implicitMeshSize.y/IG_coordTextureHeight)) + halfPixel;\n"+"  }\n"+"  vec3 pos = texture2D( IG_coordinateTexture, IG_texCoord ).rgb;\n"+"   pos = pos * (IG_bboxMax - IG_bboxMin) + IG_bboxMin;\n"+"     worldCoord = (modelMatrix * vec4(pos, 1.0)).xyz - from;\n"+"  gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+" } else {\n"+"  vec3 pos = bgCenter + bgSize * position / bgPrecisionMax;\n"+"  worldCoord = (modelMatrix * vec4(pos, 1.0)).xyz - from;\n"+"  gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+" }\n"+"}\n";}
else{shader="attribute vec3 position;\n"+"attribute vec2 texcoord;\n"+"uniform vec3 bgCenter;\n"+"uniform vec3 bgSize;\n"+"uniform float bgPrecisionMax;\n"+"uniform float writeShadowIDs;\n"+"uniform mat4 modelMatrix;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"uniform vec3 from;\n"+"varying vec3 worldCoord;\n"+"varying vec3 idCoord;\n"+"void main(void) {\n"+"    if (writeShadowIDs > 0.0) {\n"+"       float ID = (texcoord.y * 65536.0 + texcoord.x) + writeShadowIDs;\n"+"       float h = floor(ID / 256.0);\n"+"       idCoord.x = ID - (h * 256.0);\n"+"       idCoord.z = floor(h / 256.0);\n"+"       idCoord.y = h - (idCoord.z * 256.0);\n"+"       idCoord = idCoord.zyx / 255.0;\n"+"  }\n"+"    vec3 pos = bgCenter + bgSize * position / bgPrecisionMax;\n"+"    worldCoord = (modelMatrix * vec4(pos, 1.0)).xyz - from;\n"+"    gl_Position = modelViewProjectionMatrix * vec4(pos, 1.0);\n"+"}\n";}
var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[Picking24Shader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.Picking24Shader.prototype.generateFragmentShader=function(gl)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform float writeShadowIDs;\n"+"uniform float highBit;\n"+"uniform float lowBit;\n"+"uniform float sceneSize;\n"+"varying vec3 worldCoord;\n"+"varying vec3 idCoord;\n"+"void main(void) {\n"+"    vec4 col = vec4(0.0, 0.0, highBit, lowBit);\n"+"    if (writeShadowIDs > 0.0) {\n"+"       col.gba = idCoord;\n"+"  }\n"+"    col.r = length(worldCoord) / sceneSize;\n"+"    gl_FragColor = col;\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[Picking24Shader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.PickingColorShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.PickingColorShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"attribute vec3 color;\n"+"varying vec3 fragColor;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"\n"+"void main(void) {\n"+"    gl_Position = modelViewProjectionMatrix * vec4(position, 1.0);\n"+"    gl_PointSize = 2.0;\n"+"    fragColor = color;\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[PickingColorShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.PickingColorShader.prototype.generateFragmentShader=function(gl)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform float lowBit;\n"+"varying vec3 fragColor;\n"+"\n"+"void main(void) {\n"+"    gl_FragColor = vec4(fragColor, lowBit);\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[PickingColorShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.PickingTexcoordShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.PickingTexcoordShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"attribute vec2 texcoord;\n"+"varying vec3 fragColor;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+""+"void main(void) {\n"+"    gl_Position = modelViewProjectionMatrix * vec4(position, 1.0);\n"+"    fragColor = vec3(abs(texcoord.x), abs(texcoord.y), 0.0);\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[PickingTexcoordShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.PickingTexcoordShader.prototype.generateFragmentShader=function(gl)
{var shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform float lowBit;\n"+"varying vec3 fragColor;\n"+"\n"+"void main(void) {\n"+"    gl_FragColor = vec4(fragColor, lowBit);\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[PickingTexcoordShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.FrontgroundTextureShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.FrontgroundTextureShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"varying vec2 fragTexCoord;\n"+"\n"+"void main(void) {\n"+"    vec2 texCoord = (position.xy + 1.0) * 0.5;\n"+"    fragTexCoord = texCoord;\n"+"    gl_Position = vec4(position.xy, 0.0, 1.0);\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[FrontgroundTextureShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.FrontgroundTextureShader.prototype.generateFragmentShader=function(gl)
{shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform sampler2D tex;\n"+"varying vec2 fragTexCoord;\n"+"\n"+"void main(void) {\n"+"    vec4 col = texture2D(tex, fragTexCoord);\n"+"    gl_FragColor = vec4(col.rgb, 1.0);\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[FrontgroundTextureShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.BackgroundTextureShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.BackgroundTextureShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"varying vec2 fragTexCoord;\n"+"\n"+"void main(void) {\n"+"    vec2 texCoord = (position.xy + 1.0) * 0.5;\n"+"    fragTexCoord = texCoord;\n"+"    gl_Position = vec4(position.xy, 0.0, 1.0);\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[BackgroundTextureShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.BackgroundTextureShader.prototype.generateFragmentShader=function(gl)
{shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform sampler2D tex;\n"+"varying vec2 fragTexCoord;\n"+"\n"+"void main(void) {\n"+"    gl_FragColor = texture2D(tex, fragTexCoord);\n"+"}";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[BackgroundTextureShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.BackgroundSkyTextureShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.BackgroundSkyTextureShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"attribute vec2 texcoord;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"varying vec2 fragTexCoord;\n"+"\n"+"void main(void) {\n"+"    fragTexCoord = texcoord;\n"+"    gl_Position = modelViewProjectionMatrix * vec4(position, 1.0);\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[BackgroundSkyTextureShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.BackgroundSkyTextureShader.prototype.generateFragmentShader=function(gl)
{shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform sampler2D tex;\n"+"varying vec2 fragTexCoord;\n"+"\n"+"void main(void) {\n"+"    gl_FragColor = texture2D(tex, fragTexCoord);\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[BackgroundSkyTextureShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.BackgroundCubeTextureShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.BackgroundCubeTextureShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"varying vec3 fragNormal;\n"+"\n"+"void main(void) {\n"+"    fragNormal = normalize(position);\n"+"    gl_Position = modelViewProjectionMatrix * vec4(position, 1.0);\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[BackgroundCubeTextureShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.BackgroundCubeTextureShader.prototype.generateFragmentShader=function(gl)
{shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"uniform samplerCube tex;\n"+"varying vec3 fragNormal;\n"+"\n"+"float magn(float val) {\n"+"    return ((val >= 0.0) ? val : -1.0 * val);\n"+"}"+"\n"+"void main(void) {\n"+"    vec3 normal = -reflect(normalize(fragNormal), vec3(0.0,0.0,1.0));\n"+"    if (magn(normal.y) >= magn(normal.x) && magn(normal.y) >= magn(normal.z))\n"+"        normal.xz = -normal.xz;\n"+"    gl_FragColor = textureCube(tex, normal);\n"+"}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[BackgroundCubeTextureShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.shader.ShadowShader=function(gl)
{this.program=gl.createProgram();var vertexShader=this.generateVertexShader(gl);var fragmentShader=this.generateFragmentShader(gl);gl.attachShader(this.program,vertexShader);gl.attachShader(this.program,fragmentShader);gl.bindAttribLocation(this.program,0,"position");gl.linkProgram(this.program);return this.program;};x3dom.shader.ShadowShader.prototype.generateVertexShader=function(gl)
{var shader="attribute vec3 position;\n"+"uniform mat4 modelViewProjectionMatrix;\n"+"varying vec4 projCoord;\n"+"void main(void) {\n"+"   projCoord = modelViewProjectionMatrix * vec4(position, 1.0);\n"+"   gl_Position = projCoord;\n"+"}\n";var vertexShader=gl.createShader(gl.VERTEX_SHADER);gl.shaderSource(vertexShader,shader);gl.compileShader(vertexShader);if(!gl.getShaderParameter(vertexShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[ShadowShader] VertexShader "+gl.getShaderInfoLog(vertexShader));}
return vertexShader;};x3dom.shader.ShadowShader.prototype.generateFragmentShader=function(gl)
{shader="#ifdef GL_ES\n"+"  precision highp float;\n"+"#endif\n"+"\n"+"varying vec4 projCoord;\n"+"void main(void) {\n"+"    vec3 proj = (projCoord.xyz / projCoord.w);\n";if(!x3dom.caps.FP_TEXTURES){shader+="    vec4 outVal = vec4(0.0);\n"+"    float toFixed = 255.0 / 256.0;\n"+"    outVal.r = fract(proj.z * toFixed);\n"+"    outVal.g = fract(proj.z * toFixed * 255.0);\n"+"    outVal.b = fract(proj.z * toFixed * 255.0 * 255.0);\n"+"    outVal.a = fract(proj.z * toFixed * 255.0 * 255.0 * 255.0);\n"+"    gl_FragColor = outVal;\n";}else{shader+=" gl_FragColor = vec4(proj, 1.0);\n";}
shader+="}\n";var fragmentShader=gl.createShader(gl.FRAGMENT_SHADER);gl.shaderSource(fragmentShader,shader);gl.compileShader(fragmentShader);if(!gl.getShaderParameter(fragmentShader,gl.COMPILE_STATUS)){x3dom.debug.logError("[ShadowShader] FragmentShader "+gl.getShaderInfoLog(fragmentShader));}
return fragmentShader;};x3dom.gfx_webgl=(function(){function Context(ctx3d,canvas,name,x3dElem){this.ctx3d=ctx3d;this.canvas=canvas;this.name=name;this.x3dElem=x3dElem;this.IG_PositionBuffer=null;this.cache=new x3dom.Cache();}
Context.prototype.getName=function(){return this.name;};function setupContext(canvas,forbidMobileShaders,forceMobileShaders,x3dElem){var validContextNames=['moz-webgl','webkit-3d','experimental-webgl','webgl'];var ctx=null;var ctxAttribs={alpha:true,depth:true,stencil:true,antialias:true,premultipliedAlpha:false,preserveDrawingBuffer:true};for(var i=0;i<validContextNames.length;i++){try{ctx=canvas.getContext(validContextNames[i],ctxAttribs);if(ctx){var newCtx=new Context(ctx,canvas,'webgl',x3dElem);try{x3dom.debug.logInfo("\nVendor: "+ctx.getParameter(ctx.VENDOR)+", "+"Renderer: "+ctx.getParameter(ctx.RENDERER)+", "+"Version: "+ctx.getParameter(ctx.VERSION)+", "+"ShadingLangV.: "+ctx.getParameter(ctx.SHADING_LANGUAGE_VERSION)
+", "+"\nExtensions: "+ctx.getSupportedExtensions());x3dom.caps.VENDOR=ctx.getParameter(ctx.VENDOR);x3dom.caps.VERSION=ctx.getParameter(ctx.VERSION);x3dom.caps.RENDERER=ctx.getParameter(ctx.RENDERER);x3dom.caps.SHADING_LANGUAGE_VERSION=ctx.getParameter(ctx.SHADING_LANGUAGE_VERSION);x3dom.caps.RED_BITS=ctx.getParameter(ctx.RED_BITS);x3dom.caps.GREEN_BITS=ctx.getParameter(ctx.GREEN_BITS);x3dom.caps.BLUE_BITS=ctx.getParameter(ctx.BLUE_BITS);x3dom.caps.ALPHA_BITS=ctx.getParameter(ctx.ALPHA_BITS);x3dom.caps.DEPTH_BITS=ctx.getParameter(ctx.DEPTH_BITS);x3dom.caps.MAX_VERTEX_ATTRIBS=ctx.getParameter(ctx.MAX_VERTEX_ATTRIBS);x3dom.caps.MAX_VERTEX_TEXTURE_IMAGE_UNITS=ctx.getParameter(ctx.MAX_VERTEX_TEXTURE_IMAGE_UNITS);x3dom.caps.MAX_VARYING_VECTORS=ctx.getParameter(ctx.MAX_VARYING_VECTORS);x3dom.caps.MAX_VERTEX_UNIFORM_VECTORS=ctx.getParameter(ctx.MAX_VERTEX_UNIFORM_VECTORS);x3dom.caps.MAX_COMBINED_TEXTURE_IMAGE_UNITS=ctx.getParameter(ctx.MAX_COMBINED_TEXTURE_IMAGE_UNITS);x3dom.caps.MAX_TEXTURE_SIZE=ctx.getParameter(ctx.MAX_TEXTURE_SIZE);x3dom.caps.MAX_CUBE_MAP_TEXTURE_SIZE=ctx.getParameter(ctx.MAX_CUBE_MAP_TEXTURE_SIZE);x3dom.caps.COMPRESSED_TEXTURE_FORMATS=ctx.getParameter(ctx.COMPRESSED_TEXTURE_FORMATS);x3dom.caps.MAX_RENDERBUFFER_SIZE=ctx.getParameter(ctx.MAX_RENDERBUFFER_SIZE);x3dom.caps.MAX_VIEWPORT_DIMS=ctx.getParameter(ctx.MAX_VIEWPORT_DIMS);x3dom.caps.ALIASED_LINE_WIDTH_RANGE=ctx.getParameter(ctx.ALIASED_LINE_WIDTH_RANGE);x3dom.caps.ALIASED_POINT_SIZE_RANGE=ctx.getParameter(ctx.ALIASED_POINT_SIZE_RANGE);x3dom.caps.FP_TEXTURES=ctx.getExtension("OES_texture_float");x3dom.caps.EXTENSIONS=ctx.getSupportedExtensions();x3dom.caps.MOBILE=(function(a){if(/android.+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))){return true;}else{return false;}})(navigator.userAgent||navigator.vendor||window.opera);if(x3dom.caps.RENDERER.indexOf("PowerVR")>=0||navigator.appVersion.indexOf("Mobile")>-1||x3dom.caps.MAX_VARYING_VECTORS<=8||x3dom.caps.MAX_VERTEX_TEXTURE_IMAGE_UNITS<2)
{x3dom.caps.MOBILE=true;}
if(x3dom.caps.MOBILE)
{if(forbidMobileShaders){x3dom.caps.MOBILE=false;x3dom.debug.logWarning("Detected mobile graphics card! "+"But being forced to desktop shaders which might not work!");}
else{x3dom.debug.logWarning("Detected mobile graphics card! "+"Using low quality shaders without ImageGeometry support!");}}
else
{if(forceMobileShaders){x3dom.caps.MOBILE=true;x3dom.debug.logWarning("Detected desktop graphics card! "+"But being forced to mobile shaders with lower quality!");}}}
catch(ex){x3dom.debug.logWarning("Your browser probably supports an older WebGL version. "+"Please try the old mobile runtime instead:\n"+"http://www.x3dom.org/x3dom/src_mobile/x3dom.js");newCtx=null;}
return newCtx;}}
catch(e){}}
return null;}
var STATE_SWITCH_NONE=0;var STATE_SWITCH_BIND=1;var STATE_SWITCH_UNBIND=2;var STATE_SWITCH_BOTH=3;Context.prototype.setupShape=function(gl,shape,viewarea)
{var i,q=0,q5;var textures,t;var vertices,positionBuffer;var indicesBuffer,indexArray;if(shape._webgl!==undefined)
{var needFullReInit=false;if(shape._dirty.colors===true&&shape._webgl.shader.color===undefined&&shape._cf.geometry.node._mesh._colors[0].length)
{needFullReInit=true;}
if(needFullReInit)
{var spOld=shape._webgl.shader;for(q=0;q<shape._webgl.positions.length;q++)
{q5=5*q;if(spOld.position!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+1]);gl.deleteBuffer(shape._webgl.buffers[q5]);}
if(spOld.normal!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+2]);}
if(spOld.texcoord!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+3]);}
if(spOld.color!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+4]);}}
for(var inc=0;inc<shape._webgl.dynamicFields.length;inc++)
{var h_attrib=shape._webgl.dynamicFields[inc];if(spOld[h_attrib.name]!==undefined)
{gl.deleteBuffer(h_attrib.buf);}}}
if(shape._dirty.texture===true)
{if(shape._webgl.texture.length!=shape.getTextures().length)
{for(t=0;t<shape._webgl.texture.length;++t)
{shape._webgl.texture.pop();}
textures=shape.getTextures();for(t=0;t<textures.length;++t)
{shape._webgl.texture.push(new x3dom.Texture(gl,shape._nameSpace.doc,this.cache,textures[t]));}
shape._dirty.shader=true;if(shape._webgl.shader.texcoord===undefined)
shape._dirty.texCoords=true;}
else
{textures=shape.getTextures();for(t=0;t<textures.length;++t)
{if(textures[t]===shape._webgl.texture[t].node)
{shape._webgl.texture[t].update();}
else
{shape._webgl.texture[t].texture=null;shape._webgl.texture[t].node=textures[t];shape._webgl.texture[t].update();}}}
shape._dirty.texture=false;}
var oldLightsAndShadow=shape._webgl.lightsAndShadow;shape._webgl.lightsAndShadow=x3dom.Utils.checkDirtyLighting(viewarea);if(shape._webgl.lightsAndShadow[0]!=oldLightsAndShadow[0]||shape._webgl.lightsAndShadow[1]!=oldLightsAndShadow[1]||shape._dirty.shader)
{shape._webgl.shader=this.cache.getDynamicShader(gl,viewarea,shape);shape._dirty.shader=false;}
if(shape._webgl.binaryGeometry==0)
for(q=0;q<shape._webgl.positions.length;q++)
{q5=5*q;if(!needFullReInit&&shape._dirty.positions===true)
{if(shape._webgl.shader.position!==undefined)
{shape._webgl.indexes[q]=shape._cf.geometry.node._mesh._indices[q];gl.deleteBuffer(shape._webgl.buffers[q5]);indicesBuffer=gl.createBuffer();shape._webgl.buffers[q5]=indicesBuffer;indexArray=new Uint16Array(shape._webgl.indexes[q]);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,indicesBuffer);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,indexArray,gl.STATIC_DRAW);indexArray=null;shape._webgl.positions[q]=shape._cf.geometry.node._mesh._positions[q];gl.deleteBuffer(shape._webgl.buffers[q5+1]);positionBuffer=gl.createBuffer();shape._webgl.buffers[q5+1]=positionBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,shape._webgl.buffers[q5]);vertices=new Float32Array(shape._webgl.positions[q]);gl.bufferData(gl.ARRAY_BUFFER,vertices,gl.STATIC_DRAW);gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.vertexAttribPointer(shape._webgl.shader.position,shape._cf.geometry.node._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);vertices=null;}
shape._dirty.positions=false;}
if(!needFullReInit&&shape._dirty.colors===true)
{if(shape._webgl.shader.color!==undefined)
{shape._webgl.colors[q]=shape._cf.geometry.node._mesh._colors[q];gl.deleteBuffer(shape._webgl.buffers[q5+4]);colorBuffer=gl.createBuffer();shape._webgl.buffers[q5+4]=colorBuffer;colors=new Float32Array(shape._webgl.colors[q]);gl.bindBuffer(gl.ARRAY_BUFFER,colorBuffer);gl.bufferData(gl.ARRAY_BUFFER,colors,gl.STATIC_DRAW);gl.vertexAttribPointer(shape._webgl.shader.color,shape._cf.geometry.node._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);colors=null;}
shape._dirty.colors=false;}
if(!needFullReInit&&shape._dirty.normals===true)
{if(shape._webgl.shader.normal!==undefined)
{shape._webgl.normals[q]=shape._cf.geometry.node._mesh._normals[q];gl.deleteBuffer(shape._webgl.buffers[q5+2]);normalBuffer=gl.createBuffer();shape._webgl.buffers[q5+2]=normalBuffer;normals=new Float32Array(shape._webgl.normals[q]);gl.bindBuffer(gl.ARRAY_BUFFER,normalBuffer);gl.bufferData(gl.ARRAY_BUFFER,normals,gl.STATIC_DRAW);gl.vertexAttribPointer(shape._webgl.shader.normal,shape._cf.geometry.node._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);normals=null;}
shape._dirty.normals=false;}
if(!needFullReInit&&shape._dirty.texCoords===true)
{if(shape._webgl.shader.texcoord!==undefined)
{shape._webgl.texcoords[q]=shape._cf.geometry.node._mesh._texCoords[q];gl.deleteBuffer(shape._webgl.buffers[q5+3]);texCoordBuffer=gl.createBuffer();shape._webgl.buffers[q5+3]=texCoordBuffer;texCoords=new Float32Array(shape._webgl.texcoords[q]);gl.bindBuffer(gl.ARRAY_BUFFER,texCoordBuffer);gl.bufferData(gl.ARRAY_BUFFER,texCoords,gl.STATIC_DRAW);gl.vertexAttribPointer(shape._webgl.shader.texCoord,shape._cf.geometry.node._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);texCoords=null;}
shape._dirty.texCoords=false;}}
if(shape._webgl.imageGeometry!=0)
{for(t=0;t<shape._webgl.texture.length;++t)
{shape._webgl.texture[t].updateTexture();}
shape._cf.geometry.node._dirty.coord=false;shape._cf.geometry.node._dirty.normal=false;shape._cf.geometry.node._dirty.texCoord=false;shape._cf.geometry.node._dirty.color=false;shape._cf.geometry.node._dirty.index=false;}
if(!needFullReInit){return;}}
else if(!(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Text)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BinaryGeometry)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.PopGeometry)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BitLODGeometry))&&(!shape._cf.geometry.node||shape._cf.geometry.node._mesh._positions[0].length<1))
{if(x3dom.caps.MAX_VERTEX_TEXTURE_IMAGE_UNITS<2&&x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.ImageGeometry)){x3dom.debug.logError("Can't render ImageGeometry nodes with only "+
x3dom.caps.MAX_VERTEX_TEXTURE_IMAGE_UNITS+" vertex texture units. Please upgrade your GPU!");}
else{x3dom.debug.logError("NO VALID MESH OR NO VERTEX POSITIONS SET!");}
return;}
shape.unsetDirty();if(!shape._cf.appearance.node){}
if(shape._cleanupGLObjects==null)
{shape._cleanupGLObjects=function(force)
{if(this._webgl&&((arguments.length>0&&force)||this._parentNodes.length==0))
{var sp=this._webgl.shader;for(var q=0;q<this._webgl.positions.length;q++)
{var q5=5*q;if(sp.position!==undefined){gl.deleteBuffer(this._webgl.buffers[q5+1]);gl.deleteBuffer(this._webgl.buffers[q5]);}
if(sp.normal!==undefined){gl.deleteBuffer(this._webgl.buffers[q5+2]);}
if(sp.texcoord!==undefined){gl.deleteBuffer(this._webgl.buffers[q5+3]);}
if(sp.color!==undefined){gl.deleteBuffer(this._webgl.buffers[q5+4]);}}
for(var df=0;df<this._webgl.dynamicFields.length;df++)
{var attrib=this._webgl.dynamicFields[df];if(sp[attrib.name]!==undefined){gl.deleteBuffer(attrib.buf);}}
delete this._webgl;}};}
shape._webgl={positions:shape._cf.geometry.node._mesh._positions,normals:shape._cf.geometry.node._mesh._normals,texcoords:shape._cf.geometry.node._mesh._texCoords,colors:shape._cf.geometry.node._mesh._colors,indexes:shape._cf.geometry.node._mesh._indices,coordType:gl.FLOAT,normalType:gl.FLOAT,texCoordType:gl.FLOAT,colorType:gl.FLOAT,texture:[],lightsAndShadow:x3dom.Utils.checkDirtyLighting(viewarea),imageGeometry:0,binaryGeometry:0,popGeometry:0,bitLODGeometry:0};textures=shape.getTextures();for(t=0;t<textures.length;++t)
{shape._webgl.texture.push(new x3dom.Texture(gl,shape._nameSpace.doc,this.cache,textures[t]));}
shape._webgl.shader=this.cache.getDynamicShader(gl,viewarea,shape);var sp=shape._webgl.shader;var currAttribs=0;shape._webgl.buffers=[];shape._webgl.dynamicFields=[];if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.PointSet)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Polypoint2D))
{shape._webgl.primType=gl.POINTS;}
else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.IndexedLineSet)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Circle2D)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Arc2D)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Polyline2D))
{shape._webgl.primType=gl.LINES;}
else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.IndexedTriangleStripSet)&&shape._cf.geometry.node._mesh._primType.toUpperCase()=='TRIANGLESTRIP')
{shape._webgl.primType=gl.TRIANGLE_STRIP;}
else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.ImageGeometry)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BinaryGeometry)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.PopGeometry)||x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BitLODGeometry))
{shape._webgl.primType=[];for(var primCnt=0;primCnt<shape._cf.geometry.node._vf.primType.length;++primCnt)
{switch(shape._cf.geometry.node._vf.primType[primCnt].toUpperCase())
{case'POINTS':shape._webgl.primType.push(gl.POINTS);break;case'LINES':shape._webgl.primType.push(gl.LINES);break;case'TRIANGLESTRIP':shape._webgl.primType.push(gl.TRIANGLE_STRIP);break;case'TRIANGLES':default:shape._webgl.primType.push(gl.TRIANGLES);break;}}}
else
{shape._webgl.primType=gl.TRIANGLES;}
if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BinaryGeometry))
{x3dom.BinaryContainerLoader.setupBinGeo(shape,sp,gl,viewarea,this);}
else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.PopGeometry))
{x3dom.BinaryContainerLoader.setupPopGeo(shape,sp,gl,viewarea,this);}
else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BitLODGeometry))
{x3dom.BinaryContainerLoader.setupBitLODGeo(shape,sp,gl,viewarea,this);}
else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.ImageGeometry))
{x3dom.BinaryContainerLoader.setupImgGeo(shape,sp,gl,viewarea,this);}
else
{for(q=0;q<shape._webgl.positions.length;q++)
{q5=5*q;if(sp.position!==undefined)
{indicesBuffer=gl.createBuffer();shape._webgl.buffers[q5]=indicesBuffer;indexArray=new Uint16Array(shape._webgl.indexes[q]);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,indicesBuffer);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,indexArray,gl.STATIC_DRAW);indexArray=null;positionBuffer=gl.createBuffer();shape._webgl.buffers[q5+1]=positionBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);vertices=new Float32Array(shape._webgl.positions[q]);gl.bufferData(gl.ARRAY_BUFFER,vertices,gl.STATIC_DRAW);gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.vertexAttribPointer(sp.position,shape._cf.geometry.node._mesh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);vertices=null;}
if(sp.normal!==undefined||shape._webgl.normals[q])
{var normalBuffer=gl.createBuffer();shape._webgl.buffers[q5+2]=normalBuffer;var normals=new Float32Array(shape._webgl.normals[q]);gl.bindBuffer(gl.ARRAY_BUFFER,normalBuffer);gl.bufferData(gl.ARRAY_BUFFER,normals,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.normal,shape._cf.geometry.node._mesh._numNormComponents,shape._webgl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);normals=null;}
if(sp.texcoord!==undefined)
{var texcBuffer=gl.createBuffer();shape._webgl.buffers[q5+3]=texcBuffer;var texCoords=new Float32Array(shape._webgl.texcoords[q]);gl.bindBuffer(gl.ARRAY_BUFFER,texcBuffer);gl.bufferData(gl.ARRAY_BUFFER,texCoords,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.texcoord,shape._cf.geometry.node._mesh._numTexComponents,shape._webgl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);texCoords=null;}
if(sp.color!==undefined)
{var colorBuffer=gl.createBuffer();shape._webgl.buffers[q5+4]=colorBuffer;var colors=new Float32Array(shape._webgl.colors[q]);gl.bindBuffer(gl.ARRAY_BUFFER,colorBuffer);gl.bufferData(gl.ARRAY_BUFFER,colors,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.color,shape._cf.geometry.node._mesh._numColComponents,shape._webgl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);colors=null;}}
for(var df in shape._cf.geometry.node._mesh._dynamicFields)
{if(!shape._cf.geometry.node._mesh._dynamicFields.hasOwnProperty(df))
continue;var attrib=shape._cf.geometry.node._mesh._dynamicFields[df];shape._webgl.dynamicFields[currAttribs]={buf:{},name:df,numComponents:attrib.numComponents};if(sp[df]!==undefined)
{var attribBuffer=gl.createBuffer();shape._webgl.dynamicFields[currAttribs++].buf=attribBuffer;var attribs=new Float32Array(attrib.value);gl.bindBuffer(gl.ARRAY_BUFFER,attribBuffer);gl.bufferData(gl.ARRAY_BUFFER,attribs,gl.STATIC_DRAW);gl.vertexAttribPointer(sp[df],attrib.numComponents,gl.FLOAT,false,0,0);attribs=null;}}}};Context.prototype.setupScene=function(gl,bgnd){var sphere;var texture;if(bgnd._webgl!==undefined)
{if(!bgnd._dirty){return;}
if(bgnd._webgl.texture!==undefined&&bgnd._webgl.texture)
{gl.deleteTexture(bgnd._webgl.texture);}
if(bgnd._webgl.shader&&bgnd._webgl.shader.position!==undefined)
{gl.deleteBuffer(bgnd._webgl.buffers[1]);gl.deleteBuffer(bgnd._webgl.buffers[0]);}
if(bgnd._webgl.shader&&bgnd._webgl.shader.texcoord!==undefined)
{gl.deleteBuffer(bgnd._webgl.buffers[2]);}
bgnd._webgl={};}
bgnd._dirty=false;var url=bgnd.getTexUrl();var i=0;var w=1,h=1;if(url.length>0&&url[0].length>0)
{if(url.length>=6&&url[1].length>0&&url[2].length>0&&url[3].length>0&&url[4].length>0&&url[5].length>0)
{sphere=new x3dom.nodeTypes.Sphere();bgnd._webgl={positions:sphere._mesh._positions[0],indexes:sphere._mesh._indices[0],buffers:[{},{}]};bgnd._webgl.primType=gl.TRIANGLES;bgnd._webgl.shader=this.cache.getShader(gl,x3dom.shader.BACKGROUND_CUBETEXTURE);bgnd._webgl.texture=x3dom.Utils.createTextureCube(gl,bgnd._nameSpace.doc,url,true,bgnd._vf.withCredentials);}
else{bgnd._webgl={positions:[-w,-h,0,-w,h,0,w,-h,0,w,h,0],indexes:[0,1,2,3],buffers:[{},{}]};url=bgnd._nameSpace.getURL(url[0]);bgnd._webgl.texture=x3dom.Utils.createTexture2D(gl,bgnd._nameSpace.doc,url,true,bgnd._vf.withCredentials);bgnd._webgl.primType=gl.TRIANGLE_STRIP;bgnd._webgl.shader=this.cache.getShader(gl,x3dom.shader.BACKGROUND_TEXTURE);}}
else
{if(bgnd.getSkyColor().length>1||bgnd.getGroundColor().length)
{sphere=new x3dom.nodeTypes.Sphere();texture=gl.createTexture();bgnd._webgl={positions:sphere._mesh._positions[0],texcoords:sphere._mesh._texCoords[0],indexes:sphere._mesh._indices[0],buffers:[{},{},{}],texture:texture,primType:gl.TRIANGLES};var N=x3dom.Utils.nextHighestPowerOfTwo(bgnd.getSkyColor().length+bgnd.getGroundColor().length+2);N=(N<512)?512:N;var n=bgnd._vf.groundAngle.length;var tmp=[],arr=[];var colors=[],sky=[0];for(i=0;i<bgnd._vf.skyColor.length;i++){colors[i]=bgnd._vf.skyColor[i];}
for(i=0;i<bgnd._vf.skyAngle.length;i++){sky[i+1]=bgnd._vf.skyAngle[i];}
if(n>0||bgnd._vf.groundColor.length==1){if(sky[sky.length-1]<Math.PI/2){sky[sky.length]=Math.PI/2-x3dom.fields.Eps;colors[colors.length]=colors[colors.length-1];}
for(i=n-1;i>=0;i--){if((i==n-1)&&(Math.PI-bgnd._vf.groundAngle[i]<=Math.PI/2)){sky[sky.length]=Math.PI/2;colors[colors.length]=bgnd._vf.groundColor[bgnd._vf.groundColor.length-1];}
sky[sky.length]=Math.PI-bgnd._vf.groundAngle[i];colors[colors.length]=bgnd._vf.groundColor[i+1];}
if(n==0&&bgnd._vf.groundColor.length==1){sky[sky.length]=Math.PI/2;colors[colors.length]=bgnd._vf.groundColor[0];}
sky[sky.length]=Math.PI;colors[colors.length]=bgnd._vf.groundColor[0];}
else{if(sky[sky.length-1]<Math.PI){sky[sky.length]=Math.PI;colors[colors.length]=colors[colors.length-1];}}
for(i=0;i<sky.length;i++){sky[i]/=Math.PI;}
x3dom.debug.assert(sky.length==colors.length);var interp=new x3dom.nodeTypes.ColorInterpolator();interp._vf.key=new x3dom.fields.MFFloat(sky);interp._vf.keyValue=new x3dom.fields.MFColor(colors);for(i=0;i<N;i++){interp._vf.set_fraction=i/(N-1.0);interp.fieldChanged("set_fraction");tmp[i]=interp._vf.value_changed;}
tmp.reverse();for(i=0;i<tmp.length;i++){arr[3*i+0]=Math.floor(tmp[i].r*255);arr[3*i+1]=Math.floor(tmp[i].g*255);arr[3*i+2]=Math.floor(tmp[i].b*255);}
var pixels=new Uint8Array(arr);var format=gl.RGB;N=(pixels.length)/3;gl.bindTexture(gl.TEXTURE_2D,texture);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);gl.pixelStorei(gl.UNPACK_ALIGNMENT,1);gl.texImage2D(gl.TEXTURE_2D,0,format,1,N,0,format,gl.UNSIGNED_BYTE,pixels);gl.bindTexture(gl.TEXTURE_2D,null);bgnd._webgl.shader=this.cache.getShader(gl,x3dom.shader.BACKGROUND_SKYTEXTURE);}
else
{bgnd._webgl={};}}
if(bgnd._webgl.shader)
{var sp=bgnd._webgl.shader;var positionBuffer=gl.createBuffer();bgnd._webgl.buffers[1]=positionBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);var vertices=new Float32Array(bgnd._webgl.positions);gl.bufferData(gl.ARRAY_BUFFER,vertices,gl.STATIC_DRAW);gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.vertexAttribPointer(sp.position,3,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp.position);var indicesBuffer=gl.createBuffer();bgnd._webgl.buffers[0]=indicesBuffer;var indexArray=new Uint16Array(bgnd._webgl.indexes);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,indicesBuffer);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,indexArray,gl.STATIC_DRAW);vertices=null;indexArray=null;if(sp.texcoord!==undefined)
{var texcBuffer=gl.createBuffer();bgnd._webgl.buffers[2]=texcBuffer;var texcoords=new Float32Array(bgnd._webgl.texcoords);gl.bindBuffer(gl.ARRAY_BUFFER,texcBuffer);gl.bufferData(gl.ARRAY_BUFFER,texcoords,gl.STATIC_DRAW);gl.vertexAttribPointer(sp.texcoord,2,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp.texcoord);texcoords=null;}}
bgnd._webgl.render=function(gl,mat_view,mat_proj)
{var sp=bgnd._webgl.shader;var mat_scene=null;var projMatrix_22=mat_proj._22,projMatrix_23=mat_proj._23;var camPos=mat_view.e3();if((sp!==undefined&&sp!==null)&&(sp.texcoord!==undefined&&sp.texcoord!==null)&&(bgnd._webgl.texture!==undefined&&bgnd._webgl.texture!==null))
{gl.clearDepth(1.0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT|gl.STENCIL_BUFFER_BIT);gl.frontFace(gl.CCW);gl.disable(gl.CULL_FACE);gl.disable(gl.DEPTH_TEST);gl.disable(gl.BLEND);sp.bind();if(!sp.tex){sp.tex=0;}
sp.alpha=1.0;mat_proj._22=100001/99999;mat_proj._23=200000/99999;mat_view._03=0;mat_view._13=0;mat_view._23=0;mat_scene=mat_proj.mult(mat_view);sp.modelViewProjectionMatrix=mat_scene.toGL();mat_view._03=camPos.x;mat_view._13=camPos.y;mat_view._23=camPos.z;mat_proj._22=projMatrix_22;mat_proj._23=projMatrix_23;gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,bgnd._webgl.texture);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,bgnd._webgl.buffers[0]);gl.bindBuffer(gl.ARRAY_BUFFER,bgnd._webgl.buffers[1]);gl.vertexAttribPointer(sp.position,3,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp.position);gl.bindBuffer(gl.ARRAY_BUFFER,bgnd._webgl.buffers[2]);gl.vertexAttribPointer(sp.texcoord,2,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp.texcoord);try{gl.drawElements(bgnd._webgl.primType,bgnd._webgl.indexes.length,gl.UNSIGNED_SHORT,0);}
catch(e){x3dom.debug.logException("Render background: "+e);}
gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,null);gl.disableVertexAttribArray(sp.position);gl.disableVertexAttribArray(sp.texcoord);gl.clear(gl.DEPTH_BUFFER_BIT|gl.STENCIL_BUFFER_BIT);}
else if(!sp||!bgnd._webgl.texture||(bgnd._webgl.texture.textureCubeReady!==undefined&&bgnd._webgl.texture.textureCubeReady!==true))
{var bgCol=bgnd.getSkyColor().toGL();bgCol[3]=1.0-bgnd.getTransparency();gl.clearColor(bgCol[0],bgCol[1],bgCol[2],bgCol[3]);gl.clearDepth(1.0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT|gl.STENCIL_BUFFER_BIT);}
else
{gl.clearDepth(1.0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT|gl.STENCIL_BUFFER_BIT);gl.frontFace(gl.CCW);gl.disable(gl.CULL_FACE);gl.disable(gl.DEPTH_TEST);gl.disable(gl.BLEND);sp.bind();if(!sp.tex){sp.tex=0;}
if(bgnd._webgl.texture.textureCubeReady){mat_proj._22=100001/99999;mat_proj._23=200000/99999;mat_view._03=0;mat_view._13=0;mat_view._23=0;mat_scene=mat_proj.mult(mat_view);sp.modelViewProjectionMatrix=mat_scene.toGL();mat_view._03=camPos.x;mat_view._13=camPos.y;mat_view._23=camPos.z;mat_proj._22=projMatrix_22;mat_proj._23=projMatrix_23;gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_CUBE_MAP,bgnd._webgl.texture);gl.texParameteri(gl.TEXTURE_CUBE_MAP,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_CUBE_MAP,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_CUBE_MAP,gl.TEXTURE_MIN_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_CUBE_MAP,gl.TEXTURE_MAG_FILTER,gl.LINEAR);}
else{gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,bgnd._webgl.texture);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);}
gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,bgnd._webgl.buffers[0]);gl.bindBuffer(gl.ARRAY_BUFFER,bgnd._webgl.buffers[1]);gl.vertexAttribPointer(sp.position,3,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp.position);try{gl.drawElements(bgnd._webgl.primType,bgnd._webgl.indexes.length,gl.UNSIGNED_SHORT,0);}
catch(e){x3dom.debug.logException("Render background: "+e);}
gl.disableVertexAttribArray(sp.position);if(bgnd._webgl.texture.textureCubeReady){gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_CUBE_MAP,null);}
else{gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,null);}
gl.clear(gl.DEPTH_BUFFER_BIT|gl.STENCIL_BUFFER_BIT);}};};Context.prototype.setupFgnds=function(gl,scene)
{if(scene._fgnd!==undefined){return;}
var w=1,h=1;scene._fgnd={};scene._fgnd._webgl={positions:[-w,-h,0,-w,h,0,w,-h,0,w,h,0],indexes:[0,1,2,3],buffers:[{},{}]};scene._fgnd._webgl.primType=gl.TRIANGLE_STRIP;scene._fgnd._webgl.shader=this.cache.getShader(gl,x3dom.shader.FRONTGROUND_TEXTURE);var sp=scene._fgnd._webgl.shader;var positionBuffer=gl.createBuffer();scene._fgnd._webgl.buffers[1]=positionBuffer;gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);var vertices=new Float32Array(scene._fgnd._webgl.positions);gl.bufferData(gl.ARRAY_BUFFER,vertices,gl.STATIC_DRAW);gl.bindBuffer(gl.ARRAY_BUFFER,positionBuffer);gl.vertexAttribPointer(sp.position,3,gl.FLOAT,false,0,0);var indicesBuffer=gl.createBuffer();scene._fgnd._webgl.buffers[0]=indicesBuffer;var indexArray=new Uint16Array(scene._fgnd._webgl.indexes);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,indicesBuffer);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,indexArray,gl.STATIC_DRAW);vertices=null;indexArray=null;scene._fgnd._webgl.render=function(gl,tex)
{scene._fgnd._webgl.texture=tex;gl.frontFace(gl.CCW);gl.disable(gl.CULL_FACE);gl.disable(gl.DEPTH_TEST);sp.bind();if(!sp.tex){sp.tex=0;}
gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,scene._fgnd._webgl.texture);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,scene._fgnd._webgl.buffers[0]);gl.bindBuffer(gl.ARRAY_BUFFER,scene._fgnd._webgl.buffers[1]);gl.vertexAttribPointer(sp.position,3,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp.position);try{gl.drawElements(scene._fgnd._webgl.primType,scene._fgnd._webgl.indexes.length,gl.UNSIGNED_SHORT,0);}
catch(e){x3dom.debug.logException("Render foreground: "+e);}
gl.disableVertexAttribArray(sp.position);gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,null);};};Context.prototype.renderShadowPass=function(gl,scene,mat_light,mat_scene)
{gl.bindFramebuffer(gl.FRAMEBUFFER,scene._webgl.fboShadow.fbo);gl.viewport(0,0,scene._webgl.fboShadow.width,scene._webgl.fboShadow.height);gl.clearColor(1.0,1.0,1.0,1.0);gl.clearDepth(1.0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT);gl.depthFunc(gl.LEQUAL);gl.enable(gl.DEPTH_TEST);gl.enable(gl.CULL_FACE);gl.disable(gl.BLEND);var sp=scene._webgl.shadowShader;sp.bind();var i,n=scene.drawableObjects.length;for(i=0;i<n;i++)
{var trafo=scene.drawableObjects[i][0];var shape=scene.drawableObjects[i][1];var s_gl=shape._webgl;if(!s_gl||s_gl.culled===true){continue;}
var s_geo=shape._cf.geometry.node;var s_msh=s_geo._mesh;sp.modelViewMatrix=mat_light.mult(trafo).toGL();sp.modelViewProjectionMatrix=mat_scene.mult(trafo).toGL();for(var q=0,q_n=shape._webgl.positions.length;q<q_n;q++)
{var q5=5*q;var v,v_n,offset;if(shape._webgl.buffers[q5])
{gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,shape._webgl.buffers[q5]);}
if(sp.position!==undefined&&shape._webgl.buffers[q5+1])
{gl.bindBuffer(gl.ARRAY_BUFFER,shape._webgl.buffers[q5+1]);gl.vertexAttribPointer(sp.position,s_msh._numPosComponents,shape._webgl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);}
if(shape._webgl.indexes&&shape._webgl.indexes[q5])
{if(shape._webgl.imageGeometry!=0||shape._webgl.binaryGeometry<0||shape._webgl.popGeometry<0||shape._webgl.bitLODGeometry<0)
{for(v=0,offset=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{gl.drawArrays(shape._webgl.primType[v],offset,s_geo._vf.vertexCount[v]);offset+=s_geo._vf.vertexCount[v];}}
else if(shape._webgl.binaryGeometry>0||shape._webgl.popGeometry>0||shape._webgl.bitLODGeometry>0)
{for(v=0,offset=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{gl.drawElements(shape._webgl.primType[v],s_geo._vf.vertexCount[v],gl.UNSIGNED_SHORT,2*offset);offset+=s_geo._vf.vertexCount[v];}}
else if(x3dom.isa(s_geo,x3dom.nodeTypes.IndexedTriangleStripSet)&&shape._webgl.primType==gl.TRIANGLE_STRIP)
{var indOff=s_geo._indexOffset;for(v=1,v_n=indOff.length;v<v_n;v++)
{gl.drawElements(shape._webgl.primType,indOff[v]-indOff[v-1],gl.UNSIGNED_SHORT,2*indOff[v-1]);}}
else
{gl.drawElements(shape._webgl.primType,shape._webgl.indexes[q].length,gl.UNSIGNED_SHORT,0);}}
if(sp.position!==undefined){gl.disableVertexAttribArray(sp.position);}}}
gl.flush();gl.bindFramebuffer(gl.FRAMEBUFFER,null);};Context.prototype.renderPickingPass=function(gl,scene,mat_view,mat_scene,from,sceneSize,pickMode,lastX,lastY,width,height)
{gl.bindFramebuffer(gl.FRAMEBUFFER,scene._webgl.fboPick.fbo);gl.viewport(0,0,scene._webgl.fboPick.width,scene._webgl.fboPick.height);gl.clearColor(0.0,0.0,0.0,0.0);gl.clearDepth(1.0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT);gl.depthFunc(gl.LEQUAL);gl.enable(gl.DEPTH_TEST);gl.enable(gl.CULL_FACE);gl.disable(gl.BLEND);var sp=null;switch(pickMode){case 0:sp=scene._webgl.pickShader;break;case 1:sp=scene._webgl.pickColorShader;break;case 2:sp=scene._webgl.pickTexCoordShader;break;case 3:sp=scene._webgl.pickShader24;break;default:break;}
if(!sp){return;}
sp.bind();var bgCenter=new x3dom.fields.SFVec3f(0,0,0).toGL();var bgSize=new x3dom.fields.SFVec3f(1,1,1).toGL();for(var i=0,n=scene.drawableObjects.length;i<n;i++)
{var trafo=scene.drawableObjects[i][0];var shape=scene.drawableObjects[i][1];var s_gl=shape._webgl;if(shape._objectID<1||!s_gl||!shape._vf.isPickable||s_gl.culled===true){continue;}
var s_geo=shape._cf.geometry.node;var s_msh=s_geo._mesh;sp.modelMatrix=trafo.toGL();sp.modelViewProjectionMatrix=mat_scene.mult(trafo).toGL();sp.lowBit=(shape._objectID&255)/255.0;sp.highBit=(shape._objectID>>>8)/255.0;sp.from=from.toGL();sp.sceneSize=sceneSize;sp.imageGeometry=s_gl.imageGeometry;sp.writeShadowIDs=(s_gl.binaryGeometry!=0&&s_geo._vf.idsPerVertex)?(x3dom.nodeTypes.Shape.objectID+2):0;if(s_gl.coordType!=gl.FLOAT)
{if(s_gl.bitLODGeometry!=0||s_gl.popGeometry!=0||(s_msh._numPosComponents==4&&x3dom.Utils.isUnsignedType(s_geo._vf.coordType)))
sp.bgCenter=s_geo.getMin().toGL();else
sp.bgCenter=s_geo._vf.position.toGL();sp.bgSize=s_geo._vf.size.toGL();sp.bgPrecisionMax=s_geo.getPrecisionMax('coordType');}
else{sp.bgCenter=bgCenter;sp.bgSize=bgSize;sp.bgPrecisionMax=1;}
if(s_gl.colorType!=gl.FLOAT){sp.bgPrecisionColMax=s_geo.getPrecisionMax('colorType');}
if(s_gl.texCoordType!=gl.FLOAT){sp.bgPrecisionTexMax=s_geo.getPrecisionMax('texCoordType');}
if(s_gl.imageGeometry!=0&&!x3dom.caps.MOBILE)
{sp.IG_bboxMin=s_geo.getMin().toGL();sp.IG_bboxMax=s_geo.getMax().toGL();sp.IG_implicitMeshSize=s_geo._vf.implicitMeshSize.toGL();var coordTex=x3dom.Utils.findTextureByName(s_gl.texture,"IG_coords0");if(coordTex){sp.IG_coordTextureWidth=coordTex.texture.width;sp.IG_coordTextureHeight=coordTex.texture.height;}
if(s_gl.imageGeometry==1){var indexTex=x3dom.Utils.findTextureByName(s_gl.texture,"IG_index");if(indexTex){sp.IG_indexTextureWidth=indexTex.texture.width;sp.IG_indexTextureHeight=indexTex.texture.height;}
gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,indexTex.texture);gl.activeTexture(gl.TEXTURE1);gl.bindTexture(gl.TEXTURE_2D,coordTex.texture);}
else{gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,coordTex.texture);}
gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.NEAREST);var texUnit=0;if(s_geo.getIndexTexture()){if(!sp.IG_indexTexture){sp.IG_indexTexture=texUnit++;}}
if(s_geo.getCoordinateTexture(0)){if(!sp.IG_coordinateTexture){sp.IG_coordinateTexture=texUnit++;}}}
for(var q=0,q_n=s_gl.positions.length;q<q_n;q++)
{var q5=5*q;var v,v_n,offset;if(s_gl.buffers[q5])
{gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,s_gl.buffers[q5]);}
if(sp.position!==undefined&&s_gl.buffers[q5+1])
{gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+1]);gl.vertexAttribPointer(sp.position,s_msh._numPosComponents,s_gl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);}
if(sp.texcoord!==undefined&&s_gl.buffers[q5+3])
{gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+3]);gl.vertexAttribPointer(sp.texcoord,s_msh._numTexComponents,s_gl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
if(sp.color!==undefined&&s_gl.buffers[q5+4])
{gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+4]);gl.vertexAttribPointer(sp.color,s_msh._numColComponents,s_gl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}
if(shape.isSolid()){gl.enable(gl.CULL_FACE);if(shape.isCCW()){gl.frontFace(gl.CCW);}
else{gl.frontFace(gl.CW);}}
else{gl.disable(gl.CULL_FACE);}
if(s_gl.indexes&&s_gl.indexes[q])
{if(s_gl.imageGeometry!=0||s_gl.binaryGeometry<0||s_gl.popGeometry<0||s_gl.bitLODGeometry<0)
{if(s_gl.bitLODGeometry!=0&&s_geo._vf.normalPerVertex===false)
{var totalVertexCount=0;for(v=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{if(s_gl.primType[v]==gl.TRIANGLES){totalVertexCount+=s_geo._vf.vertexCount[v];}
else if(s_gl.primType[v]==gl.TRIANGLE_STRIP){totalVertexCount+=(s_geo._vf.vertexCount[v]-2)*3;}}
gl.drawArrays(gl.TRIANGLES,0,totalVertexCount);}
else
{for(v=0,offset=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{gl.drawArrays(s_gl.primType[v],offset,s_geo._vf.vertexCount[v]);offset+=s_geo._vf.vertexCount[v];}}}
else if(s_gl.binaryGeometry>0||s_gl.popGeometry>0||s_gl.bitLODGeometry>0)
{for(v=0,offset=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{gl.drawElements(s_gl.primType[v],s_geo._vf.vertexCount[v],gl.UNSIGNED_SHORT,2*offset);offset+=s_geo._vf.vertexCount[v];}}
else if(x3dom.isa(s_geo,x3dom.nodeTypes.IndexedTriangleStripSet)&&s_gl.primType==gl.TRIANGLE_STRIP)
{var indOff=s_geo._indexOffset;for(v=1,v_n=indOff.length;v<v_n;v++)
{gl.drawElements(s_gl.primType,indOff[v]-indOff[v-1],gl.UNSIGNED_SHORT,2*indOff[v-1]);}}
else
{gl.drawElements(s_gl.primType,s_gl.indexes[q].length,gl.UNSIGNED_SHORT,0);}}
if(s_gl.imageGeometry!=0&&!x3dom.caps.MOBILE)
{gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,null);if(s_gl.imageGeometry==1){gl.activeTexture(gl.TEXTURE1);gl.bindTexture(gl.TEXTURE_2D,null);}}
if(sp.position!==undefined&&s_gl.buffers[q5+1]){gl.disableVertexAttribArray(sp.position);}
if(sp.texcoord!==undefined&&s_gl.buffers[q5+3]){gl.disableVertexAttribArray(sp.texcoord);}
if(sp.color!==undefined&&s_gl.buffers[q5+4]){gl.disableVertexAttribArray(sp.color);}}}
gl.flush();try{var x=lastX*scene._webgl.pickScale,y=scene._webgl.fboPick.height-1-lastY*scene._webgl.pickScale;var data=new Uint8Array(4*width*height);gl.readPixels(x,y,width,height,gl.RGBA,gl.UNSIGNED_BYTE,data);scene._webgl.fboPick.pixelData=data;}
catch(se){scene._webgl.fboPick.pixelData=[];x3dom.debug.logException(se+" (cannot pick)");}
gl.bindFramebuffer(gl.FRAMEBUFFER,null);};Context.prototype.renderShape=function(transform,shape,viewarea,slights,numLights,mat_view,mat_scene,mat_light,mat_proj,gl,oneShadowExistsAlready,stateSwitchMode)
{if(!shape||!shape._webgl){x3dom.debug.logError("[Context|RenderShape] No valid Shape!");return;}
var s_gl=shape._webgl;var sp=s_gl.shader;if(!sp){x3dom.debug.logError("[Context|RenderShape] No Shader is set!");return;}
{sp.bind();}
var s_app=shape._cf.appearance.node;var s_geo=shape._cf.geometry.node;var s_msh=s_geo._mesh;var scene=viewarea._scene;var tex=null;if(s_gl.coordType!=gl.FLOAT)
{if(s_gl.popGeometry===0&&(s_gl.bitLODGeometry!=0||(s_msh._numPosComponents==4&&x3dom.Utils.isUnsignedType(s_geo._vf.coordType)))){sp.bgCenter=s_geo.getMin().toGL();}
else{sp.bgCenter=s_geo._vf.position.toGL();}
sp.bgSize=s_geo._vf.size.toGL();sp.bgPrecisionMax=s_geo.getPrecisionMax('coordType');}
if(s_gl.colorType!=gl.FLOAT){sp.bgPrecisionColMax=s_geo.getPrecisionMax('colorType');}
if(s_gl.texCoordType!=gl.FLOAT){sp.bgPrecisionTexMax=s_geo.getPrecisionMax('texCoordType');}
if(s_gl.normalType!=gl.FLOAT){sp.bgPrecisionNorMax=s_geo.getPrecisionMax('normalType');}
if(s_gl.imageGeometry!=0)
{sp.IG_bboxMin=s_geo.getMin().toGL();sp.IG_bboxMax=s_geo.getMax().toGL();sp.IG_implicitMeshSize=s_geo._vf.implicitMeshSize.toGL();tex=x3dom.Utils.findTextureByName(s_gl.texture,"IG_coords0");if(tex){sp.IG_coordTextureWidth=tex.texture.width;sp.IG_coordTextureHeight=tex.texture.height;}
if(s_gl.imageGeometry==1){tex=x3dom.Utils.findTextureByName(s_gl.texture,"IG_index");if(tex){sp.IG_indexTextureWidth=tex.texture.width;sp.IG_indexTextureHeight=tex.texture.height;}}
tex=null;}
var fog=scene.getFog();if(fog){sp.fogColor=fog._vf.color.toGL();sp.fogRange=fog._vf.visibilityRange;sp.fogType=(fog._vf.fogType=="LINEAR")?0.0:1.0;}
var mat=s_app?s_app._cf.material.node:null;var shader=s_app?s_app._shader:null;{if(mat||s_gl.csshader){if(s_gl.csshader){sp.diffuseColor=shader._vf.diffuseFactor.toGL();sp.specularColor=shader._vf.specularFactor.toGL();sp.emissiveColor=shader._vf.emissiveFactor.toGL();sp.shininess=shader._vf.shininessFactor;sp.ambientIntensity=(shader._vf.ambientFactor.x+
shader._vf.ambientFactor.y+
shader._vf.ambientFactor.z)/3;sp.transparency=1.0-shader._vf.alphaFactor;}
else if(mat){sp.diffuseColor=mat._vf.diffuseColor.toGL();sp.specularColor=mat._vf.specularColor.toGL();sp.emissiveColor=mat._vf.emissiveColor.toGL();sp.shininess=mat._vf.shininess;sp.ambientIntensity=mat._vf.ambientIntensity;sp.transparency=mat._vf.transparency;}}
else{sp.diffuseColor=[1.0,1.0,1.0];sp.specularColor=[0.0,0.0,0.0];sp.emissiveColor=[0.0,0.0,0.0];sp.shininess=0.0;sp.ambientIntensity=1.0;sp.transparency=0.0;}}
if(shader)
{if(x3dom.isa(shader,x3dom.nodeTypes.ComposedShader))
{for(var fName in shader._vf){if(shader._vf.hasOwnProperty(fName)&&fName!=='language'){var field=shader._vf[fName];if(field){if(field.toGL){sp[fName]=field.toGL();}
else{sp[fName]=field;}}}}}
else if(x3dom.isa(shader,x3dom.nodeTypes.CommonSurfaceShader)){s_gl.csshader=shader;}}
if(numLights>0)
{for(var p=0;p<numLights;p++)
{var light_transform=mat_view.mult(slights[p].getCurrentTransform());if(x3dom.isa(slights[p],x3dom.nodeTypes.DirectionalLight)){sp['light'+p+'_Type']=0.0;sp['light'+p+'_On']=(slights[p]._vf.on)?1.0:0.0;sp['light'+p+'_Color']=slights[p]._vf.color.toGL();sp['light'+p+'_Intensity']=slights[p]._vf.intensity;sp['light'+p+'_AmbientIntensity']=slights[p]._vf.ambientIntensity;sp['light'+p+'_Direction']=light_transform.multMatrixVec(slights[p]._vf.direction).toGL();sp['light'+p+'_Attenuation']=[1.0,1.0,1.0];sp['light'+p+'_Location']=[1.0,1.0,1.0];sp['light'+p+'_Radius']=0.0;sp['light'+p+'_BeamWidth']=0.0;sp['light'+p+'_CutOffAngle']=0.0;sp['light'+p+'_ShadowIntensity']=slights[p]._vf.shadowIntensity;}
else if(x3dom.isa(slights[p],x3dom.nodeTypes.PointLight)){sp['light'+p+'_Type']=1.0;sp['light'+p+'_On']=(slights[p]._vf.on)?1.0:0.0;sp['light'+p+'_Color']=slights[p]._vf.color.toGL();sp['light'+p+'_Intensity']=slights[p]._vf.intensity;sp['light'+p+'_AmbientIntensity']=slights[p]._vf.ambientIntensity;sp['light'+p+'_Direction']=[1.0,1.0,1.0];sp['light'+p+'_Attenuation']=slights[p]._vf.attenuation.toGL();sp['light'+p+'_Location']=light_transform.multMatrixPnt(slights[p]._vf.location).toGL();sp['light'+p+'_Radius']=slights[p]._vf.radius;sp['light'+p+'_BeamWidth']=0.0;sp['light'+p+'_CutOffAngle']=0.0;sp['light'+p+'_ShadowIntensity']=slights[p]._vf.shadowIntensity;}
else if(x3dom.isa(slights[p],x3dom.nodeTypes.SpotLight)){sp['light'+p+'_Type']=2.0;sp['light'+p+'_On']=(slights[p]._vf.on)?1.0:0.0;sp['light'+p+'_Color']=slights[p]._vf.color.toGL();sp['light'+p+'_Intensity']=slights[p]._vf.intensity;sp['light'+p+'_AmbientIntensity']=slights[p]._vf.ambientIntensity;sp['light'+p+'_Direction']=light_transform.multMatrixVec(slights[p]._vf.direction).toGL();sp['light'+p+'_Attenuation']=slights[p]._vf.attenuation.toGL();sp['light'+p+'_Location']=light_transform.multMatrixPnt(slights[p]._vf.location).toGL();sp['light'+p+'_Radius']=slights[p]._vf.radius;sp['light'+p+'_BeamWidth']=slights[p]._vf.beamWidth;sp['light'+p+'_CutOffAngle']=slights[p]._vf.cutOffAngle;sp['light'+p+'_ShadowIntensity']=slights[p]._vf.shadowIntensity;}}}
var nav=scene.getNavigationInfo();if(nav._vf.headlight){numLights=(numLights)?numLights:0;sp['light'+numLights+'_Type']=0.0;sp['light'+numLights+'_On']=1.0;sp['light'+numLights+'_Color']=[1.0,1.0,1.0];sp['light'+numLights+'_Intensity']=1.0;sp['light'+numLights+'_AmbientIntensity']=0.0;sp['light'+numLights+'_Direction']=[0.0,0.0,-1.0];sp['light'+numLights+'_Attenuation']=[1.0,1.0,1.0];sp['light'+numLights+'_Location']=[1.0,1.0,1.0];sp['light'+numLights+'_Radius']=0.0;sp['light'+numLights+'_BeamWidth']=0.0;sp['light'+numLights+'_CutOffAngle']=0.0;sp['light'+numLights+'_ShadowIntensity']=0.0;}
var model_view=mat_view.mult(transform);var model_view_inv=model_view.inverse();sp.modelViewMatrix=model_view.toGL();sp.viewMatrix=mat_view.toGL();sp.normalMatrix=model_view_inv.transpose().toGL();sp.modelViewMatrixInverse=model_view_inv.toGL();sp.projectionMatrix=mat_proj.toGL();sp.modelViewProjectionMatrix=mat_scene.mult(transform).toGL();if(s_gl.popGeometry){this.updatePopState(s_geo,sp,s_gl,scene,viewarea,model_view,this.x3dElem.runtime.fps);}
{for(var cnt=0,cnt_n=s_gl.texture.length;cnt<cnt_n;cnt++)
{tex=s_gl.texture[cnt];gl.activeTexture(gl.TEXTURE0+cnt);gl.bindTexture(tex.type,tex.texture);gl.texParameteri(tex.type,gl.TEXTURE_WRAP_S,tex.wrapS);gl.texParameteri(tex.type,gl.TEXTURE_WRAP_T,tex.wrapT);gl.texParameteri(tex.type,gl.TEXTURE_MAG_FILTER,tex.magFilter);gl.texParameteri(tex.type,gl.TEXTURE_MIN_FILTER,tex.minFilter);if(tex.genMipMaps){gl.generateMipmap(tex.type);}
if(!shader||shader&&!x3dom.isa(shader,x3dom.nodeTypes.ComposedShader)){if(!sp[tex.samplerName])
sp[tex.samplerName]=cnt;}}
if(s_app&&s_app._cf.textureTransform.node){var texTrafo=s_app.texTransformMatrix();sp.texTrafoMatrix=texTrafo.toGL();}
if(oneShadowExistsAlready)
{if(!sp.sh_tex){sp.sh_tex=cnt;}
gl.activeTexture(gl.TEXTURE0+cnt);gl.bindTexture(gl.TEXTURE_2D,scene._webgl.fboShadow.tex);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);sp.matPV=mat_light.mult(transform).toGL();}}
var attrib=null;for(var df=0,df_n=s_gl.dynamicFields.length;df<df_n;df++)
{attrib=s_gl.dynamicFields[df];if(sp[attrib.name]!==undefined){gl.bindBuffer(gl.ARRAY_BUFFER,attrib.buf);gl.vertexAttribPointer(sp[attrib.name],attrib.numComponents,gl.FLOAT,false,0,0);gl.enableVertexAttribArray(sp[attrib.name]);}}
{if(shape.isSolid()){gl.enable(gl.CULL_FACE);if(shape.isCCW()){gl.frontFace(gl.CCW);}else{gl.frontFace(gl.CW);}}else{gl.disable(gl.CULL_FACE);}}
var i,i_n,offset;for(var q=0,q_n=s_gl.positions.length;q<q_n;q++)
{var q5=5*q;if(s_gl.buffers[q5]){gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,s_gl.buffers[q5]);}
if(sp.position!==undefined&&s_gl.buffers[q5+1]){gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+1]);gl.vertexAttribPointer(sp.position,s_msh._numPosComponents,s_gl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);}
if(sp.normal!==undefined&&s_gl.buffers[q5+2]){gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+2]);gl.vertexAttribPointer(sp.normal,s_msh._numNormComponents,s_gl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);}
if(sp.texcoord!==undefined&&s_gl.buffers[q5+3]){gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+3]);gl.vertexAttribPointer(sp.texcoord,s_msh._numTexComponents,s_gl.texCoordType,false,shape._texCoordStrideOffset[0],shape._texCoordStrideOffset[1]);gl.enableVertexAttribArray(sp.texcoord);}
if(sp.color!==undefined&&s_gl.buffers[q5+4]){gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+4]);gl.vertexAttribPointer(sp.color,s_msh._numColComponents,s_gl.colorType,false,shape._colorStrideOffset[0],shape._colorStrideOffset[1]);gl.enableVertexAttribArray(sp.color);}
if(s_gl.popGeometry!==0&&s_gl.buffers[q5+5]){gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+5]);gl.vertexAttribPointer(sp.PG_vertexID,1,gl.FLOAT,false,4,0);gl.enableVertexAttribArray(sp.PG_vertexID);}
if(viewarea._points!==undefined&&viewarea._points>0)
{var polyMode=(viewarea._points==1)?gl.POINTS:gl.LINES;if(s_gl.imageGeometry!=0||s_gl.binaryGeometry<0||s_gl.popGeometry<0||s_gl.bitLODGeometry<0)
{for(i=0,offset=0,i_n=s_geo._vf.vertexCount.length;i<i_n;i++)
{gl.drawArrays(polyMode,offset,s_geo._vf.vertexCount[i]);offset+=s_geo._vf.vertexCount[i];}}
else if(s_gl.binaryGeometry>0||s_gl.popGeometry>0||s_gl.bitLODGeometry>0)
{for(i=0,offset=0,i_n=s_geo._vf.vertexCount.length;i<i_n;i++)
{gl.drawElements(polyMode,s_geo._vf.vertexCount[i],gl.UNSIGNED_SHORT,2*offset);offset+=s_geo._vf.vertexCount[i];}}
else
{gl.drawElements(polyMode,s_gl.indexes[q].length,gl.UNSIGNED_SHORT,0);}}
else if(sp.position!==undefined&&s_gl.buffers[q5+1])
{if(s_gl.primType==gl.POINTS&&(typeof s_gl.primType).toString()!="object")
{gl.drawArrays(gl.POINTS,0,s_gl.positions[q].length/3);}
else if(s_gl.indexes&&s_gl.indexes[q])
{if(s_gl.imageGeometry!=0||s_gl.binaryGeometry<0||s_gl.popGeometry<0||s_gl.bitLODGeometry<0)
{if(s_gl.bitLODGeometry!=0&&s_geo._vf.normalPerVertex===false)
{var totalVertexCount=0;for(i=0,i_n=s_geo._vf.vertexCount.length;i<i_n;i++)
{if(s_gl.primType[i]==gl.TRIANGLES){totalVertexCount+=s_geo._vf.vertexCount[i];}
else if(s_gl.primType[i]==gl.TRIANGLE_STRIP){totalVertexCount+=(s_geo._vf.vertexCount[i]-2)*3;}}
gl.drawArrays(gl.TRIANGLES,0,totalVertexCount);}
else
{for(i=0,offset=0,i_n=s_geo._vf.vertexCount.length;i<i_n;i++)
{gl.drawArrays(s_gl.primType[i],offset,s_geo._vf.vertexCount[i]);offset+=s_geo._vf.vertexCount[i];}}}
else if(s_gl.binaryGeometry>0||s_gl.popGeometry>0||s_gl.bitLODGeometry>0)
{for(i=0,offset=0,i_n=s_geo._vf.vertexCount.length;i<i_n;i++)
{gl.drawElements(s_gl.primType[i],s_geo._vf.vertexCount[i],gl.UNSIGNED_SHORT,2*offset);offset+=s_geo._vf.vertexCount[i];}}
else if(x3dom.isa(s_geo,x3dom.nodeTypes.IndexedTriangleStripSet)&&s_gl.primType==gl.TRIANGLE_STRIP)
{var indOff=s_geo._indexOffset;for(i=1,i_n=indOff.length;i<i_n;i++)
{gl.drawElements(s_gl.primType,indOff[i]-indOff[i-1],gl.UNSIGNED_SHORT,2*indOff[i-1]);}}
else
{gl.drawElements(s_gl.primType,s_gl.indexes[q].length,gl.UNSIGNED_SHORT,0);}}}
if(sp.position!==undefined){gl.disableVertexAttribArray(sp.position);}
if(sp.normal!==undefined){gl.disableVertexAttribArray(sp.normal);}
if(sp.texcoord!==undefined){gl.disableVertexAttribArray(sp.texcoord);}
if(sp.color!==undefined){gl.disableVertexAttribArray(sp.color);}
if(s_gl.popGeometry!==0&&sp.PG_vertexID!==undefined){gl.disableVertexAttribArray(sp.PG_vertexID);}}
if(s_gl.indexes&&s_gl.indexes[0])
{if(s_gl.imageGeometry!=0)
{for(i=0,i_n=s_geo._vf.vertexCount.length;i<i_n;i++)
{if(s_gl.primType[i]==gl.TRIANGLE_STRIP)
this.numFaces+=(s_geo._vf.vertexCount[i]-2);else
this.numFaces+=(s_geo._vf.vertexCount[i]/3);}}
else
{this.numFaces+=s_msh._numFaces;}}
if(s_gl.imageGeometry!=0)
{i_n=s_geo._vf.vertexCount.length;for(i=0;i<i_n;i++)
this.numCoords+=s_geo._vf.vertexCount[i];this.numDrawCalls+=i_n;}
else if(s_gl.binaryGeometry!=0||s_gl.popGeometry!=0||s_gl.bitLODGeometry!=0)
{this.numCoords+=s_msh._numCoords;this.numDrawCalls+=s_geo._vf.vertexCount.length;}
else
{this.numCoords+=s_msh._numCoords;if(x3dom.isa(s_geo,x3dom.nodeTypes.IndexedTriangleStripSet)&&s_gl.primType==gl.TRIANGLE_STRIP)
{this.numDrawCalls+=s_geo._indexOffset.length;}
else
{this.numDrawCalls+=s_gl.positions.length;}}
for(df=0,df_n=s_gl.dynamicFields.length;df<df_n;df++)
{attrib=s_gl.dynamicFields[df];if(sp[attrib.name]!==undefined){gl.disableVertexAttribArray(sp[attrib.name]);}}
{var s_gl_tex=s_gl.texture;cnt_n=s_gl_tex?s_gl_tex.length:0;for(cnt=0;cnt<cnt_n;cnt++)
{if(!s_gl_tex[cnt])
continue;if(s_app&&s_app._cf.texture.node)
{tex=s_app._cf.texture.node.getTexture(cnt);gl.activeTexture(gl.TEXTURE0+cnt);if(x3dom.isa(tex,x3dom.nodeTypes.X3DEnvironmentTextureNode)){gl.bindTexture(gl.TEXTURE_CUBE_MAP,null);}
else{gl.bindTexture(gl.TEXTURE_2D,null);}}}
if(oneShadowExistsAlready){gl.activeTexture(gl.TEXTURE0+cnt);gl.bindTexture(gl.TEXTURE_2D,null);}}};Context.prototype.updatePopState=function(popGeo,sp,s_gl,scene,viewarea,model_view,currFps)
{var tol=x3dom.nodeTypes.PopGeometry.ErrorToleranceFactor*popGeo._vf.precisionFactor;if(currFps<=1||viewarea.isMoving()){tol*=x3dom.nodeTypes.PopGeometry.PrecisionFactorOnMove;}
var currentLOD=16;if(tol>0)
{var viewpoint=scene.getViewpoint();var imgPlaneHeightAtDistOne=viewpoint.getImgPlaneHeightAtDistOne();var near=viewpoint.getNear();var center=model_view.multMatrixPnt(popGeo._vf.position);var dist=Math.max(-center.z-popGeo._volRadius,near);var projPixelLength=dist*(imgPlaneHeightAtDistOne/viewarea._height);var arg=(2*popGeo._volLargestRadius)/(tol*projPixelLength);currentLOD=Math.ceil(Math.log(arg)/0.693147180559945);currentLOD=(currentLOD<1)?1:((currentLOD>16)?16:currentLOD);}
var minPrec=popGeo._vf.minPrecisionLevel,maxPrec=popGeo._vf.maxPrecisionLevel;currentLOD=(minPrec!=-1&&currentLOD<minPrec)?minPrec:currentLOD;currentLOD=(maxPrec!=-1&&currentLOD>maxPrec)?maxPrec:currentLOD;var currentLOD_min=(s_gl.levelsAvailable<currentLOD)?s_gl.levelsAvailable:currentLOD;currentLOD=currentLOD_min;if(tol<=1)
currentLOD=(currentLOD==popGeo.getNumLevels())?16:currentLOD;var hasIndex=popGeo._vf.indexedRendering;var p_msh=popGeo._mesh;p_msh._numCoords=0;p_msh._numFaces=0;for(var i=0;i<currentLOD_min;++i){var numVerticesAtLevel_i=s_gl.numVerticesAtLevel[i];p_msh._numCoords+=numVerticesAtLevel_i;p_msh._numFaces+=(hasIndex?popGeo.getNumIndicesByLevel(i):numVerticesAtLevel_i)/3;}
x3dom.nodeTypes.PopGeometry.numRenderedVerts+=p_msh._numCoords;x3dom.nodeTypes.PopGeometry.numRenderedTris+=p_msh._numFaces;p_msh.currentLOD=currentLOD;popGeo.adaptVertexCount(hasIndex?p_msh._numFaces*3:p_msh._numCoords);sp.PG_bbMin=popGeo._bbMinBySize;sp.PG_numAnchorVertices=popGeo._vf.numAnchorVertices;sp.PG_bbMaxModF=popGeo._vf.bbMaxModF.toGL();sp.PG_bboxShiftVec=popGeo._vf.bbShiftVec.toGL();sp.PG_precisionLevel=currentLOD;sp.PG_powPrecision=x3dom.nodeTypes.PopGeometry.powLUT[currentLOD-1];};Context.prototype.pickValue=function(viewarea,x,y,buttonState,viewMat,sceneMat)
{var gl=this.ctx3d;var scene=viewarea._scene;if(gl===null||scene===null||!scene._webgl||!scene.drawableObjects||scene._vf.pickMode.toLowerCase()==="box")
{return false;}
var mat_view,mat_scene;if(arguments.length>4){mat_view=viewMat;mat_scene=sceneMat;}
else{mat_view=viewarea._last_mat_view;mat_scene=viewarea._last_mat_scene;}
var pickMode=(scene._vf.pickMode.toLowerCase()==="color")?1:((scene._vf.pickMode.toLowerCase()==="texcoord")?2:((scene._vf.pickMode.toLowerCase()==="idbuf24")?3:0));var min=scene._lastMin;var max=scene._lastMax;var from=mat_view.inverse().e3();var _min=x3dom.fields.SFVec3f.copy(from);var _max=x3dom.fields.SFVec3f.copy(from);if(_min.x>min.x){_min.x=min.x;}
if(_min.y>min.y){_min.y=min.y;}
if(_min.z>min.z){_min.z=min.z;}
if(_max.x<max.x){_max.x=max.x;}
if(_max.y<max.y){_max.y=max.y;}
if(_max.z<max.z){_max.z=max.z;}
min.setValues(_min);max.setValues(_max);var sceneSize=max.subtract(min).length();this.renderPickingPass(gl,scene,mat_view,mat_scene,from,sceneSize,pickMode,x,y,2,2);var index=0;if(index>=0&&scene._webgl.fboPick.pixelData&&index<scene._webgl.fboPick.pixelData.length)
{var pickPos=new x3dom.fields.SFVec3f(0,0,0);var pickNorm=new x3dom.fields.SFVec3f(0,0,1);var objId=scene._webgl.fboPick.pixelData[index+3];var pixelOffset=1.0/scene._webgl.pickScale;var denom=1.0/256.0;var dist,line,lineoff,right,up;if(pickMode==0)
{objId+=256*scene._webgl.fboPick.pixelData[index+2];dist=(scene._webgl.fboPick.pixelData[index+0]/255.0)*denom+
(scene._webgl.fboPick.pixelData[index+1]/255.0);line=viewarea.calcViewRay(x,y);pickPos=line.pos.add(line.dir.multiply(dist*sceneSize));index=4;dist=(scene._webgl.fboPick.pixelData[index+0]/255.0)*denom+
(scene._webgl.fboPick.pixelData[index+1]/255.0);lineoff=viewarea.calcViewRay(x+pixelOffset,y);right=lineoff.pos.add(lineoff.dir.multiply(dist*sceneSize));right=right.subtract(pickPos).normalize();index=8;dist=(scene._webgl.fboPick.pixelData[index+0]/255.0)*denom+
(scene._webgl.fboPick.pixelData[index+1]/255.0);lineoff=viewarea.calcViewRay(x,y-pixelOffset);up=lineoff.pos.add(lineoff.dir.multiply(dist*sceneSize));up=up.subtract(pickPos).normalize();pickNorm=right.cross(up).normalize();}
else if(pickMode==3)
{objId+=256*scene._webgl.fboPick.pixelData[index+2]+
65536*scene._webgl.fboPick.pixelData[index+1];dist=scene._webgl.fboPick.pixelData[index+0]/255.0;line=viewarea.calcViewRay(x,y);pickPos=line.pos.add(line.dir.multiply(dist*sceneSize));index=4;dist=scene._webgl.fboPick.pixelData[index+0]/255.0;lineoff=viewarea.calcViewRay(x+pixelOffset,y);right=lineoff.pos.add(lineoff.dir.multiply(dist*sceneSize));right=right.subtract(pickPos).normalize();index=8;dist=scene._webgl.fboPick.pixelData[index+0]/255.0;lineoff=viewarea.calcViewRay(x,y-pixelOffset);up=lineoff.pos.add(lineoff.dir.multiply(dist*sceneSize));up=up.subtract(pickPos).normalize();pickNorm=right.cross(up).normalize();}
else
{pickPos.x=scene._webgl.fboPick.pixelData[index+0];pickPos.y=scene._webgl.fboPick.pixelData[index+1];pickPos.z=scene._webgl.fboPick.pixelData[index+2];}
var baseID=x3dom.nodeTypes.Shape.objectID+2;if(objId>=baseID){objId-=baseID;viewarea._pickingInfo.pickPos=pickPos;viewarea._pick.setValues(pickPos);viewarea._pickingInfo.pickNorm=pickNorm;viewarea._pickNorm.setValues(pickNorm);viewarea._pickingInfo.pickObj=null;viewarea._pickingInfo.lastClickObj=null;var eventType="shadowObjectIdChanged";try{if(scene._xmlNode&&(scene._xmlNode["on"+eventType]||scene._xmlNode.hasAttribute("on"+eventType)||scene._listeners[eventType])){var event={target:scene._xmlNode,type:eventType,button:buttonState,layerX:x,layerY:y,shadowObjectId:objId,worldX:pickPos.x,worldY:pickPos.y,worldZ:pickPos.z,normalX:pickNorm.x,normalY:pickNorm.y,normalZ:pickNorm.z,hitPnt:pickPos.toGL(),hitObject:scene._xmlNode,cancelBubble:false,stopPropagation:function(){this.cancelBubble=true;},preventDefault:function(){this.cancelBubble=true;}};scene.callEvtHandler(("on"+eventType),event);}}
catch(e){x3dom.debug.logException(e);}
if(scene._shadowIdMap&&scene._shadowIdMap.mapping){var shIds=scene._shadowIdMap.mapping[objId].usage;for(var c=0;c<shIds.length;c++){var shObj=scene._nameSpace.defMap[shIds[c]];if(shObj.doIntersect(line)){viewarea._pickingInfo.pickObj=shObj;break;}}}}
else if(objId>0){viewarea._pickingInfo.pickPos=pickPos;viewarea._pickingInfo.pickNorm=pickNorm;viewarea._pickingInfo.pickObj=x3dom.nodeTypes.Shape.idMap.nodeID[objId];}
else{viewarea._pickingInfo.pickObj=null;viewarea._pickingInfo.lastClickObj=null;}}
return true;};Context.prototype.pickRect=function(viewarea,x1,y1,x2,y2)
{var gl=this.ctx3d;var scene=viewarea?viewarea._scene:null;if(gl===null||scene===null||!scene._webgl||!scene.drawableObjects)
return false;var from=viewarea._last_mat_view.inverse().e3();var sceneSize=scene._lastMax.subtract(scene._lastMin).length();var x=(x1<=x2)?x1:x2;var y=(y1>=y2)?y1:y2;var width=(1+Math.abs(x2-x1))*scene._webgl.pickScale;var height=(1+Math.abs(y2-y1))*scene._webgl.pickScale;this.renderPickingPass(gl,scene,viewarea._last_mat_view,viewarea._last_mat_scene,from,sceneSize,0,x,y,(width<1)?1:width,(height<1)?1:height);var index=0;var pickedObjects=[];for(index=0;scene._webgl.fboPick.pixelData&&index<scene._webgl.fboPick.pixelData.length;index+=4)
{var objId=scene._webgl.fboPick.pixelData[index+3]+
scene._webgl.fboPick.pixelData[index+2]*256;if(objId>0)
pickedObjects.push(objId);}
pickedObjects.sort();var pickedObjectsTemp=(function(arr){var a=[],l=arr.length;for(var i=0;i<l;i++){for(var j=i+1;j<l;j++){if(arr[i]===arr[j])
j=++i;}
a.push(arr[i]);}
return a;})(pickedObjects);pickedObjects=pickedObjectsTemp;var pickedNodes=[];for(index=0;index<pickedObjects.length;index++)
{var obj=pickedObjects[index];obj=x3dom.nodeTypes.Shape.idMap.nodeID[obj];obj=(obj&&obj._xmlNode)?obj._xmlNode:null;if(obj)
pickedNodes.push(obj);}
return pickedNodes;};Context.prototype.renderScene=function(viewarea)
{var gl=this.ctx3d;var scene=viewarea._scene;if(gl===null||scene===null)
{return;}
var rentex=viewarea._doc._nodeBag.renderTextures;var rt_tex,rtl_i,rtl_n=rentex.length;var type=gl.UNSIGNED_BYTE;if(x3dom.caps.FP_TEXTURES){type=gl.FLOAT;}
if(!scene._webgl)
{scene._webgl={};this.setupFgnds(gl,scene);scene._webgl.pickScale=0.5;scene._webgl._currFboWidth=Math.round(this.canvas.width*scene._webgl.pickScale);scene._webgl._currFboHeight=Math.round(this.canvas.height*scene._webgl.pickScale);scene._webgl.fboPick=this.initFbo(gl,scene._webgl._currFboWidth,scene._webgl._currFboHeight,true,gl.UNSIGNED_BYTE);scene._webgl.fboPick.pixelData=null;scene._webgl.pickShader=this.cache.getShader(gl,x3dom.shader.PICKING);scene._webgl.pickShader24=this.cache.getShader(gl,x3dom.shader.PICKING_24);scene._webgl.pickColorShader=this.cache.getShader(gl,x3dom.shader.PICKING_COLOR);scene._webgl.pickTexCoordShader=this.cache.getShader(gl,x3dom.shader.PICKING_TEXCOORD);scene._webgl.normalShader=this.cache.getShader(gl,x3dom.shader.NORMAL);scene._webgl.fboShadow=this.initFbo(gl,1024,1024,false,type);scene._webgl.shadowShader=this.cache.getShader(gl,x3dom.shader.SHADOW);for(rtl_i=0;rtl_i<rtl_n;rtl_i++){rt_tex=rentex[rtl_i];rt_tex._webgl={};rt_tex._webgl.fbo=this.initFbo(gl,rt_tex._vf.dimensions[0],rt_tex._vf.dimensions[1],false,type);}
var min=x3dom.fields.SFVec3f.MAX();var max=x3dom.fields.SFVec3f.MIN();scene.getVolume(min,max);scene._lastMin=min;scene._lastMax=max;viewarea._last_mat_view=x3dom.fields.SFMatrix4f.identity();viewarea._last_mat_proj=x3dom.fields.SFMatrix4f.identity();viewarea._last_mat_scene=x3dom.fields.SFMatrix4f.identity();this._calledViewpointChangedHandler=false;}
else
{var fboWidth=Math.round(this.canvas.width*scene._webgl.pickScale);var fboHeight=Math.round(this.canvas.height*scene._webgl.pickScale);if(scene._webgl._currFboWidth!==fboWidth||scene._webgl._currFboHeight!==fboHeight)
{scene._webgl._currFboWidth=fboWidth;scene._webgl._currFboHeight=fboHeight;scene._webgl.fboPick=this.initFbo(gl,fboWidth,fboHeight,true,scene._webgl.fboPick.typ);scene._webgl.fboPick.pixelData=null;x3dom.debug.logInfo("Refreshed picking FBO to size ("+fboWidth+", "+fboHeight+")");}
for(rtl_i=0;rtl_i<rtl_n;rtl_i++){rt_tex=rentex[rtl_i];if(rt_tex._webgl&&rt_tex._webgl.fbo)
continue;rt_tex._webgl={};rt_tex._webgl.fbo=this.initFbo(gl,rt_tex._vf.dimensions[0],rt_tex._vf.dimensions[1],false,type);}}
var bgnd=scene.getBackground();this.setupScene(gl,bgnd);this.numFaces=0;this.numCoords=0;this.numDrawCalls=0;var needShapeSetup=false;if(!scene._vf.isStaticHierarchy)
scene.drawableObjects=null;if(!scene.drawableObjects)
{needShapeSetup=true;scene.drawableObjects=[];scene.drawableObjects.LODs=[];scene.drawableObjects.Billboards=[];scene.drawableObjects.cnt=0;x3dom.Utils.startMeasure('traverse');scene.collectDrawableObjects(x3dom.fields.SFMatrix4f.identity(),scene.drawableObjects);var traverseTime=x3dom.Utils.stopMeasure('traverse');this.x3dElem.runtime.addMeasurement('TRAVERSE',traverseTime);}
var mat_proj=viewarea.getProjectionMatrix();var mat_view=viewarea.getViewMatrix();if(!this._calledViewpointChangedHandler||!viewarea._last_mat_view.equals(mat_view))
{var e_viewpoint=scene.getViewpoint();var e_eventType="viewpointChanged";try{if(e_viewpoint._xmlNode&&(e_viewpoint._xmlNode["on"+e_eventType]||e_viewpoint._xmlNode.hasAttribute("on"+e_eventType)||e_viewpoint._listeners[e_eventType]))
{var e_viewtrafo=e_viewpoint.getCurrentTransform();e_viewtrafo=e_viewtrafo.inverse().mult(mat_view);var e_mat=e_viewtrafo.inverse();var e_rotation=new x3dom.fields.Quaternion(0,0,1,0);e_rotation.setValue(e_mat);var e_translation=e_mat.e3();var e_event={target:e_viewpoint._xmlNode,type:e_eventType,matrix:e_viewtrafo,position:e_translation,orientation:e_rotation.toAxisAngle(),cancelBubble:false,stopPropagation:function(){this.cancelBubble=true;},preventDefault:function(){this.cancelBubble=true;}};e_viewpoint.callEvtHandler(("on"+e_eventType),e_event);this._calledViewpointChangedHandler=true;}}
catch(e_e){x3dom.debug.logException(e_e);}}
viewarea._last_mat_view=mat_view;viewarea._last_mat_proj=mat_proj;var mat_scene=mat_proj.mult(mat_view);viewarea._last_mat_scene=mat_scene;x3dom.Utils.startMeasure('sorting');var zPos=[],sortKeyArr=[],zPosTransp={};var sortKeyProp="";var requireSortKeySort=false;var requireTransparencySort=false;var requireTransparencySortKeySort=false;var i,m,n=scene.drawableObjects.length;var center,trafo,obj3d;for(i=0;i<n;i++)
{trafo=scene.drawableObjects[i][0];obj3d=scene.drawableObjects[i][1];this.setupShape(gl,obj3d,viewarea);if(scene._vf.sortTrans==true)
{center=obj3d.getCenter();center=trafo.multMatrixPnt(center);center=mat_view.multMatrixPnt(center);var obj3dApp=obj3d._cf.appearance.node;var sortType=obj3dApp?obj3dApp._vf.sortType:"opaque";var sortKey=obj3dApp?obj3dApp._vf.sortKey:0;if(sortType.toLowerCase()==="opaque"){zPos.push([i,center.z,sortKey]);if(sortKey!=0)
requireSortKeySort=true;}
else{sortKeyProp=sortKey.toString();if(zPosTransp[sortKeyProp]===undefined)
zPosTransp[sortKeyProp]=[];zPosTransp[sortKeyProp].push([i,center.z,sortKey]);sortKeyArr.push(sortKey);requireTransparencySort=true;if(sortKey!=0)
requireTransparencySortKeySort=true;}}
else{zPos.push([i]);}}
if(scene._vf.sortTrans==true)
{if(requireSortKeySort)
zPos.sort(function(a,b){return a[2]-b[2];});if(requireTransparencySortKeySort)
{sortKeyArr.sort(function(a,b){return a-b;});var sortKeyArrTemp=(function(arr){var a=[],l=arr.length;for(var i=0;i<l;i++){for(var j=i+1;j<l;j++){if(arr[i]===arr[j])
j=++i;}
a.push(arr[i]);}
return a;})(sortKeyArr);sortKeyArr=sortKeyArrTemp;}
else{sortKeyArr=[0];}
for(var sortKeyArrIt=0,sortKeyArrN=sortKeyArr.length;sortKeyArrIt<sortKeyArrN;++sortKeyArrIt)
{sortKeyProp=sortKeyArr[sortKeyArrIt];var zPosTranspArr=zPosTransp[sortKeyProp];if(requireTransparencySort)
zPosTranspArr.sort(function(a,b){return a[1]-b[1];});zPos.push.apply(zPos,zPosTranspArr);}}
var sortTime=x3dom.Utils.stopMeasure('sorting');this.x3dElem.runtime.addMeasurement('SORT',sortTime);m=scene.drawableObjects.Billboards.length;n=scene.drawableObjects.LODs.length;if(m||n){center=new x3dom.fields.SFVec3f(0,0,0);center=mat_view.inverse().multMatrixPnt(center);}
for(i=0;i<n;i++)
{trafo=scene.drawableObjects.LODs[i][0];obj3d=scene.drawableObjects.LODs[i][1];if(obj3d){obj3d._eye=trafo.inverse().multMatrixPnt(center);}}
for(i=0;i<m;i++)
{trafo=scene.drawableObjects.Billboards[i][0];obj3d=scene.drawableObjects.Billboards[i][1];if(obj3d){var mat_view_model=mat_view.mult(trafo);obj3d._eye=trafo.inverse().multMatrixPnt(center);obj3d._eyeViewUp=new x3dom.fields.SFVec3f(mat_view_model._10,mat_view_model._11,mat_view_model._12);obj3d._eyeLook=new x3dom.fields.SFVec3f(mat_view_model._20,mat_view_model._21,mat_view_model._22);}}
var slights=viewarea.getLights();var numLights=slights.length;var oneShadowExistsAlready=false;var mat_light;var hasShadow=false;for(var p=0;p<numLights;p++){if(slights[p]._vf.shadowIntensity>0.0&&!oneShadowExistsAlready){hasShadow=true;oneShadowExistsAlready=true;x3dom.Utils.startMeasure('shadow');var lightMatrix=viewarea.getLightMatrix()[0];mat_light=viewarea.getWCtoLCMatrix(lightMatrix);this.renderShadowPass(gl,scene,lightMatrix,mat_light);var shadowTime=x3dom.Utils.stopMeasure('shadow');this.x3dElem.runtime.addMeasurement('SHADOW',shadowTime);}}
if(!hasShadow){this.x3dElem.runtime.removeMeasurement('SHADOW');}
for(rtl_i=0;rtl_i<rtl_n;rtl_i++){this.renderRTPass(gl,viewarea,rentex[rtl_i]);}
x3dom.Utils.startMeasure('render');gl.viewport(0,0,this.canvas.width,this.canvas.height);bgnd._webgl.render(gl,mat_view,mat_proj);gl.depthMask(true);gl.depthFunc(gl.LEQUAL);gl.enable(gl.DEPTH_TEST);gl.enable(gl.CULL_FACE);gl.blendFuncSeparate(gl.SRC_ALPHA,gl.ONE_MINUS_SRC_ALPHA,gl.ONE,gl.ONE);gl.enable(gl.BLEND);x3dom.nodeTypes.PopGeometry.numRenderedVerts=0;x3dom.nodeTypes.PopGeometry.numRenderedTris=0;var view_frustum=viewarea.getViewfrustum(mat_scene);if(view_frustum)
{var box=new x3dom.fields.BoxVolume();var unculledObjects=0;}
var prevRenderedAppearance=null;var nextRenderedAppearance=null;for(i=0,n=zPos.length;i<n;i++)
{var obj=scene.drawableObjects[zPos[i][0]];if(view_frustum&&obj[1]._webgl)
{obj[1].getVolume(box.min,box.max);box.transform(obj[0]);if(!view_frustum.intersect(box)){obj[1]._webgl.culled=true;continue;}
else{obj[1]._webgl.culled=false;}
unculledObjects++;}
var needEnableBlending=false;var needEnableDepthMask=false;var shapeApp=obj[1]._cf.appearance.node;if(i<n-1)
nextRenderedAppearance=scene.drawableObjects[zPos[i+1][0]][1]._cf.appearance.node;else
nextRenderedAppearance=null;var stateSwitchMode=STATE_SWITCH_BOTH;{if(shapeApp&&shapeApp._cf.blendMode.node&&shapeApp._cf.blendMode.node._vf.srcFactor.toLowerCase()==="none"&&shapeApp._cf.blendMode.node._vf.destFactor.toLowerCase()==="none")
{needEnableBlending=true;gl.disable(gl.BLEND);}
if(shapeApp&&shapeApp._cf.depthMode.node&&shapeApp._cf.depthMode.node._vf.readOnly===true)
{needEnableDepthMask=true;gl.depthMask(false);}}
this.renderShape(obj[0],obj[1],viewarea,slights,numLights,mat_view,mat_scene,mat_light,mat_proj,gl,oneShadowExistsAlready,stateSwitchMode);{if(needEnableBlending){gl.enable(gl.BLEND);}
if(needEnableDepthMask){gl.depthMask(true);}}
prevRenderedAppearance=shapeApp;}
if(view_frustum)
viewarea._numRenderedNodes=unculledObjects;else
viewarea._numRenderedNodes=zPos.length;gl.disable(gl.BLEND);gl.disable(gl.DEPTH_TEST);if(viewarea._visDbgBuf!==undefined&&viewarea._visDbgBuf)
{if(scene._vf.pickMode.toLowerCase().indexOf("idbuf")==0||scene._vf.pickMode.toLowerCase()=="color"||scene._vf.pickMode.toLowerCase()=="texcoord"){gl.viewport(0,3*this.canvas.height/4,this.canvas.width/4,this.canvas.height/4);scene._fgnd._webgl.render(gl,scene._webgl.fboPick.tex);}
if(oneShadowExistsAlready){gl.viewport(this.canvas.width/4,3*this.canvas.height/4,this.canvas.width/4,this.canvas.height/4);scene._fgnd._webgl.render(gl,scene._webgl.fboShadow.tex);}
for(rtl_i=0;rtl_i<rtl_n;rtl_i++){rt_tex=rentex[rtl_i];gl.viewport(rtl_i*(this.canvas.width/8),5*this.canvas.height/8,(rtl_i+1)*(this.canvas.width/8),this.canvas.height/8);scene._fgnd._webgl.render(gl,rt_tex._webgl.fbo.tex);}}
gl.finish();var renderTime=x3dom.Utils.stopMeasure('render');this.x3dElem.runtime.addMeasurement('RENDER',renderTime);this.x3dElem.runtime.addMeasurement('DRAW',renderTime/zPos.length);this.x3dElem.runtime.addInfo('#NODES:',scene.drawableObjects.cnt);this.x3dElem.runtime.addInfo('#SHAPES:',viewarea._numRenderedNodes);this.x3dElem.runtime.addInfo("#DRAWS:",this.numDrawCalls);this.x3dElem.runtime.addInfo("#POINTS:",this.numCoords);this.x3dElem.runtime.addInfo("#TRIS:",this.numFaces);};Context.prototype.renderRTPass=function(gl,viewarea,rt)
{switch(rt._vf.update.toUpperCase())
{case"NONE":return;case"NEXT_FRAME_ONLY":if(!rt._needRenderUpdate){return;}
rt._needRenderUpdate=false;break;case"ALWAYS":default:break;}
var scene=viewarea._scene;var bgnd=null;var mat_view=rt.getViewMatrix();var mat_proj=rt.getProjectionMatrix();var mat_scene=mat_proj.mult(mat_view);var lightMatrix=viewarea.getLightMatrix()[0];var mat_light=viewarea.getWCtoLCMatrix(lightMatrix);var i,n,m=rt._cf.excludeNodes.nodes.length;var arr=new Array(m);for(i=0;i<m;i++){var render=rt._cf.excludeNodes.nodes[i]._vf.render;if(render===undefined){arr[i]=-1;}
else{if(render===true){arr[i]=1;}else{arr[i]=0;}}
rt._cf.excludeNodes.nodes[i]._vf.render=false;}
gl.bindFramebuffer(gl.FRAMEBUFFER,rt._webgl.fbo.fbo);gl.viewport(0,0,rt._webgl.fbo.width,rt._webgl.fbo.height);if(rt._cf.background.node===null)
{gl.clearColor(0,0,0,1);gl.clearDepth(1.0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT|gl.STENCIL_BUFFER_BIT);}
else if(rt._cf.background.node===scene.getBackground())
{bgnd=scene.getBackground();bgnd._webgl.render(gl,mat_view,mat_proj);}
else
{bgnd=rt._cf.background.node;this.setupScene(gl,bgnd);bgnd._webgl.render(gl,mat_view,mat_proj);}
gl.depthFunc(gl.LEQUAL);gl.enable(gl.DEPTH_TEST);gl.enable(gl.CULL_FACE);gl.blendFuncSeparate(gl.SRC_ALPHA,gl.ONE_MINUS_SRC_ALPHA,gl.ONE,gl.ONE);gl.enable(gl.BLEND);var slights=viewarea.getLights();var numLights=slights.length;var oneShadowExistsAlready=false;var transform,shape;var locScene=rt._cf.scene.node;var needEnableBlending,needEnableDepthMask;if(!locScene||locScene===scene)
{n=scene.drawableObjects.length;if(rt._vf.showNormals)
{this.renderNormals(gl,scene,scene._webgl.normalShader,mat_view,mat_scene);}
else
for(i=0;i<n;i++)
{transform=scene.drawableObjects[i][0];shape=scene.drawableObjects[i][1];if(!shape._vf.render){continue;}
needEnableBlending=false;needEnableDepthMask=false;if(shape._cf.appearance.node)
{appearance=shape._cf.appearance.node;var stateSwitchMode=STATE_SWITCH_BOTH;if(appearance._cf.blendMode.node&&appearance._cf.blendMode.node._vf.srcFactor.toLowerCase()==="none"&&appearance._cf.blendMode.node._vf.destFactor.toLowerCase()==="none")
{needEnableBlending=true;gl.disable(gl.BLEND);}
if(appearance._cf.depthMode.node&&appearance._cf.depthMode.node._vf.readOnly===true)
{needEnableDepthMask=true;gl.depthMask(false);}}
this.renderShape(transform,shape,viewarea,slights,numLights,mat_view,mat_scene,mat_light,mat_proj,gl,oneShadowExistsAlready,stateSwitchMode);if(needEnableBlending){gl.enable(gl.BLEND);}
if(needEnableDepthMask){gl.depthMask(true);}}}
else
{locScene.drawableObjects=[];locScene.drawableObjects.cnt=0;locScene.collectDrawableObjects(locScene.transformMatrix(x3dom.fields.SFMatrix4f.identity()),locScene.drawableObjects);n=locScene.drawableObjects.length;if(rt._vf.showNormals)
{for(i=0;i<n;i++)
{shape=locScene.drawableObjects[i][1];if(shape._vf.render)
this.setupShape(gl,shape,viewarea);}
this.renderNormals(gl,locScene,scene._webgl.normalShader,mat_view,mat_scene);}
else
for(i=0;i<n;i++)
{transform=locScene.drawableObjects[i][0];shape=locScene.drawableObjects[i][1];if(!shape._vf.render){continue;}
this.setupShape(gl,shape,viewarea);needEnableBlending=false;needEnableDepthMask=false;if(shape._cf.appearance.node){appearance=shape._cf.appearance.node;stateSwitchMode=STATE_SWITCH_BOTH;if(appearance._cf.blendMode.node&&appearance._cf.blendMode.node._vf.srcFactor.toLowerCase()==="none"&&appearance._cf.blendMode.node._vf.destFactor.toLowerCase()==="none")
{needEnableBlending=true;gl.disable(gl.BLEND);}
if(appearance._cf.depthMode.node&&appearance._cf.depthMode.node._vf.readOnly===true)
{needEnableDepthMask=true;gl.depthMask(false);}}
this.renderShape(transform,shape,viewarea,slights,numLights,mat_view,mat_scene,mat_light,mat_proj,gl,oneShadowExistsAlready,stateSwitchMode);if(needEnableBlending){gl.enable(gl.BLEND);}
if(needEnableDepthMask){gl.depthMask(true);}}}
gl.disable(gl.BLEND);gl.disable(gl.DEPTH_TEST);gl.flush();gl.bindFramebuffer(gl.FRAMEBUFFER,null);for(i=0;i<m;i++){if(arr[i]!==0){rt._cf.excludeNodes.nodes[i]._vf.render=true;}}};Context.prototype.renderNormals=function(gl,scene,sp,mat_view,mat_scene)
{if(!sp){return;}
gl.depthFunc(gl.LEQUAL);gl.enable(gl.DEPTH_TEST);gl.enable(gl.CULL_FACE);gl.disable(gl.BLEND);sp.bind();var bgCenter=new x3dom.fields.SFVec3f(0,0,0).toGL();var bgSize=new x3dom.fields.SFVec3f(1,1,1).toGL();for(var i=0,n=scene.drawableObjects.length;i<n;i++)
{var trafo=scene.drawableObjects[i][0];var shape=scene.drawableObjects[i][1];var s_gl=shape._webgl;if(!s_gl||s_gl.culled===true){continue;}
var s_geo=shape._cf.geometry.node;var s_msh=s_geo._mesh;var model_view_inv=mat_view.mult(trafo).inverse();sp.normalMatrix=model_view_inv.transpose().toGL();sp.modelViewProjectionMatrix=mat_scene.mult(trafo).toGL();sp.imageGeometry=s_gl.imageGeometry;if(s_gl.coordType!=gl.FLOAT)
{if(s_gl.bitLODGeometry!=0||s_gl.popGeometry!=0||(s_msh._numPosComponents==4&&x3dom.Utils.isUnsignedType(s_geo._vf.coordType)))
sp.bgCenter=s_geo.getMin().toGL();else
sp.bgCenter=s_geo._vf.position.toGL();sp.bgSize=s_geo._vf.size.toGL();sp.bgPrecisionMax=s_geo.getPrecisionMax('coordType');}
else{sp.bgCenter=bgCenter;sp.bgSize=bgSize;sp.bgPrecisionMax=1;}
if(s_gl.normalType!=gl.FLOAT){sp.bgPrecisionNorMax=s_geo.getPrecisionMax('normalType');}
else{sp.bgPrecisionNorMax=1;}
if(s_gl.imageGeometry!=0&&!x3dom.caps.MOBILE)
{sp.IG_bboxMin=s_geo.getMin().toGL();sp.IG_bboxMax=s_geo.getMax().toGL();sp.IG_implicitMeshSize=s_geo._vf.implicitMeshSize.toGL();var coordTex=x3dom.Utils.findTextureByName(s_gl.texture,"IG_coords0");if(coordTex){sp.IG_coordTextureWidth=coordTex.texture.width;sp.IG_coordTextureHeight=coordTex.texture.height;}
if(s_gl.imageGeometry==1){var indexTex=x3dom.Utils.findTextureByName(s_gl.texture,"IG_index");if(indexTex){sp.IG_indexTextureWidth=indexTex.texture.width;sp.IG_indexTextureHeight=indexTex.texture.height;}
gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,indexTex.texture);gl.activeTexture(gl.TEXTURE1);gl.bindTexture(gl.TEXTURE_2D,coordTex.texture);}
else{gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,coordTex.texture);}
gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.NEAREST);var texUnit=0;if(s_geo.getIndexTexture()){if(!sp.IG_indexTexture){sp.IG_indexTexture=texUnit++;}}
if(s_geo.getCoordinateTexture(0)){if(!sp.IG_coordinateTexture){sp.IG_coordinateTexture=texUnit++;}}}
for(var q=0,q_n=s_gl.positions.length;q<q_n;q++)
{var q5=5*q;var v,v_n,offset;if(s_gl.buffers[q5])
{gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,s_gl.buffers[q5]);}
if(sp.position!==undefined&&s_gl.buffers[q5+1])
{gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+1]);gl.vertexAttribPointer(sp.position,s_msh._numPosComponents,s_gl.coordType,false,shape._coordStrideOffset[0],shape._coordStrideOffset[1]);gl.enableVertexAttribArray(sp.position);}
if(sp.normal!==undefined&&s_gl.buffers[q5+2]){gl.bindBuffer(gl.ARRAY_BUFFER,s_gl.buffers[q5+2]);gl.vertexAttribPointer(sp.normal,s_msh._numNormComponents,s_gl.normalType,false,shape._normalStrideOffset[0],shape._normalStrideOffset[1]);gl.enableVertexAttribArray(sp.normal);}
if(shape.isSolid()){gl.enable(gl.CULL_FACE);if(shape.isCCW()){gl.frontFace(gl.CCW);}
else{gl.frontFace(gl.CW);}}
else{gl.disable(gl.CULL_FACE);}
if(s_gl.indexes&&s_gl.indexes[q])
{if(s_gl.imageGeometry!=0||s_gl.binaryGeometry<0||s_gl.popGeometry<0||s_gl.bitLODGeometry<0)
{if(s_gl.bitLODGeometry!=0&&s_geo._vf.normalPerVertex===false)
{var totalVertexCount=0;for(v=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{if(s_gl.primType[v]==gl.TRIANGLES){totalVertexCount+=s_geo._vf.vertexCount[v];}
else if(s_gl.primType[v]==gl.TRIANGLE_STRIP){totalVertexCount+=(s_geo._vf.vertexCount[v]-2)*3;}}
gl.drawArrays(gl.TRIANGLES,0,totalVertexCount);}
else
{for(v=0,offset=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{gl.drawArrays(s_gl.primType[v],offset,s_geo._vf.vertexCount[v]);offset+=s_geo._vf.vertexCount[v];}}}
else if(s_gl.binaryGeometry>0||s_gl.popGeometry>0||s_gl.bitLODGeometry>0)
{for(v=0,offset=0,v_n=s_geo._vf.vertexCount.length;v<v_n;v++)
{gl.drawElements(s_gl.primType[v],s_geo._vf.vertexCount[v],gl.UNSIGNED_SHORT,2*offset);offset+=s_geo._vf.vertexCount[v];}}
else if(x3dom.isa(s_geo,x3dom.nodeTypes.IndexedTriangleStripSet)&&s_gl.primType==gl.TRIANGLE_STRIP)
{var indOff=s_geo._indexOffset;for(v=1,v_n=indOff.length;v<v_n;v++)
{gl.drawElements(s_gl.primType,indOff[v]-indOff[v-1],gl.UNSIGNED_SHORT,2*indOff[v-1]);}}
else
{gl.drawElements(s_gl.primType,s_gl.indexes[q].length,gl.UNSIGNED_SHORT,0);}}
if(s_gl.imageGeometry!=0&&!x3dom.caps.MOBILE)
{gl.activeTexture(gl.TEXTURE0);gl.bindTexture(gl.TEXTURE_2D,null);if(s_gl.imageGeometry==1){gl.activeTexture(gl.TEXTURE1);gl.bindTexture(gl.TEXTURE_2D,null);}}
if(sp.position!==undefined){gl.disableVertexAttribArray(sp.position);}
if(sp.normal!==undefined){gl.disableVertexAttribArray(sp.normal);}}}};Context.prototype.shutdown=function(viewarea)
{var gl=this.ctx3d;var attrib;var scene;if(gl===null||scene===null||!scene||scene.drawableObjects===null){return;}
scene=viewarea._scene;this.cache.Release();scene.collectDrawableObjects(x3dom.fields.SFMatrix4f.identity(),scene.drawableObjects);var bgnd=scene.getBackground();if(bgnd._webgl.texture!==undefined&&bgnd._webgl.texture)
{gl.deleteTexture(bgnd._webgl.texture);}
if(bgnd._webgl.shader.position!==undefined)
{gl.deleteBuffer(bgnd._webgl.buffers[1]);gl.deleteBuffer(bgnd._webgl.buffers[0]);}
for(var i=0,n=scene.drawableObjects.length;i<n;i++)
{var shape=scene.drawableObjects[i][1];var sp=shape._webgl.shader;for(var q=0;q<shape._webgl.positions.length;q++)
{var q5=5*q;if(sp.position!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+1]);gl.deleteBuffer(shape._webgl.buffers[q5]);}
if(sp.normal!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+2]);}
if(sp.texcoord!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+3]);}
if(sp.color!==undefined)
{gl.deleteBuffer(shape._webgl.buffers[q5+4]);}}
for(var df=0;df<shape._webgl.dynamicFields.length;df++)
{attrib=shape._webgl.dynamicFields[df];if(sp[attrib.name]!==undefined)
{gl.deleteBuffer(attrib.buf);}}
shape._webgl=null;}};Context.prototype.emptyTexImage2D=function(gl,internalFormat,width,height,format,type)
{try{gl.texImage2D(gl.TEXTURE_2D,0,internalFormat,width,height,0,format,type,null);}
catch(e){var bytes=3;switch(internalFormat)
{case gl.DEPTH_COMPONENT:bytes=3;break;case gl.ALPHA:bytes=1;break;case gl.RGB:bytes=3;break;case gl.RGBA:bytes=4;break;case gl.LUMINANCE:bytes=1;break;case gl.LUMINANCE_ALPHA:bytes=2;break;}
var pixels=new Uint8Array(width*height*bytes);gl.texImage2D(gl.TEXTURE_2D,0,internalFormat,width,height,0,format,type,pixels);}};Context.prototype.initTex=function(gl,w,h,nearest,type)
{var tex=gl.createTexture();gl.bindTexture(gl.TEXTURE_2D,tex);this.emptyTexImage2D(gl,gl.RGBA,w,h,gl.RGBA,type);if(nearest){gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.NEAREST);}
else{gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.LINEAR);}
gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);gl.bindTexture(gl.TEXTURE_2D,null);tex.width=w;tex.height=h;return tex;};Context.prototype.initFbo=function(gl,w,h,nearest,type)
{var fbo=gl.createFramebuffer();var rb=gl.createRenderbuffer();var tex=this.initTex(gl,w,h,nearest,type);gl.bindFramebuffer(gl.FRAMEBUFFER,fbo);gl.bindRenderbuffer(gl.RENDERBUFFER,rb);gl.renderbufferStorage(gl.RENDERBUFFER,gl.DEPTH_COMPONENT16,w,h);gl.bindRenderbuffer(gl.RENDERBUFFER,null);gl.framebufferTexture2D(gl.FRAMEBUFFER,gl.COLOR_ATTACHMENT0,gl.TEXTURE_2D,tex,0);gl.framebufferRenderbuffer(gl.FRAMEBUFFER,gl.DEPTH_ATTACHMENT,gl.RENDERBUFFER,rb);gl.bindFramebuffer(gl.FRAMEBUFFER,null);var status=gl.checkFramebufferStatus(gl.FRAMEBUFFER);if(status!=gl.FRAMEBUFFER_COMPLETE)
x3dom.debug.logWarning("[Context|InitFBO] FBO-Status: "+status);var r={fbo:fbo,rbo:rb,tex:tex,width:w,height:h,typ:type};return r;};return setupContext;})();x3dom.bridge={setFlashReady:function(driver,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.isFlashReady=true;x3dom.debug.logInfo('Flash is ready for rendering ('+driver+')');},onMouseDown:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onMousePress(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onMouseUp:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onMouseRelease(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onMouseOver:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onMouseOver(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onMouseOut:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onMouseOut(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onDoubleClick:function(x,y,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onDoubleClick(x3dCanvas.gl,x,y);x3dCanvas.doc.needRender=true;x3dom.debug.logInfo("dblClick");},onMouseDrag:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onDrag(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onMouseMove:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onMove(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onMouseWheel:function(x,y,button,canvas){var x3dCanvas=x3dom.canvases[canvas];x3dCanvas.doc.onDrag(x3dCanvas.gl,x,y,button);x3dCanvas.doc.needRender=true;},onKeyDown:function(charCode,canvas){var x3dCanvas=x3dom.canvases[canvas];var keysEnabled=x3dCanvas.x3dElem.getAttribute("keysEnabled");if(!keysEnabled||keysEnabled.toLowerCase()==="true"){x3dCanvas.doc.onKeyPress(charCode);}
x3dCanvas.doc.needRender=true;},setBBox:function(id,center,size){var shape=x3dom.nodeTypes.Shape.idMap.nodeID[id];shape._vf.bboxCenter.setValues(new x3dom.fields.SFVec3f(center.x,center.y,center.z));shape._vf.bboxSize.setValues(new x3dom.fields.SFVec3f(size.x,size.y,size.z));},setShapeDirty:function(id){var shape=x3dom.nodeTypes.Shape.idMap.nodeID[id];shape.setAllDirty();}};x3dom.gfx_flash=(function(){function Context(object,name){this.object=object;this.name=name;this.isAlreadySet=false;};function setupContext(object){return new Context(object,'flash');};Context.prototype.getName=function(){return this.name;};Context.prototype.renderScene=function(viewarea){var scene=viewarea._scene;if(viewarea._last_mat_view==undefined){viewarea._last_mat_view=x3dom.fields.SFMatrix4f.identity();}
var mat_view=viewarea.getViewMatrix();this.setupScene(scene,viewarea);var background=scene.getBackground();this.setupBackground(background);scene.drawableObjects=null;scene.drawableObjects=[];scene.drawableObjects.LODs=[];scene.drawableObjects.Billboards=[];scene.collectDrawableObjects(x3dom.fields.SFMatrix4f.identity(),scene.drawableObjects);var numDrawableObjects=scene.drawableObjects.length;if(numDrawableObjects>0)
{var RefList=[];for(var i=0;i<numDrawableObjects;i++)
{var trafo=scene.drawableObjects[i][0];var obj3d=scene.drawableObjects[i][1];if(RefList[obj3d._objectID]!=undefined){RefList[obj3d._objectID]++;}else{RefList[obj3d._objectID]=0;}
this.setupShape(obj3d,trafo,RefList[obj3d._objectID]);}}
var numLOD=scene.drawableObjects.LODs.length;var numBillboard=scene.drawableObjects.Billboards.length;if(numLOD||numBillboard){center=new x3dom.fields.SFVec3f(0,0,0);center=mat_view.inverse().multMatrixPnt(center);}
for(var i=0;i<numLOD;i++)
{trafo=scene.drawableObjects.LODs[i][0];obj3d=scene.drawableObjects.LODs[i][1];if(obj3d){obj3d._eye=trafo.inverse().multMatrixPnt(center);}}
for(i=0;i<numBillboard;i++)
{trafo=scene.drawableObjects.Billboards[i][0];obj3d=scene.drawableObjects.Billboards[i][1];if(obj3d){var mat_view_model=mat_view.mult(trafo);obj3d._eye=trafo.inverse().multMatrixPnt(center);obj3d._eyeViewUp=new x3dom.fields.SFVec3f(mat_view_model._10,mat_view_model._11,mat_view_model._12);obj3d._eyeLook=new x3dom.fields.SFVec3f(mat_view_model._20,mat_view_model._21,mat_view_model._22);}}
this.object.renderScene();};Context.prototype.setupScene=function(scene,viewarea){var mat_view=viewarea.getViewMatrix();if(!viewarea._last_mat_view.equals(mat_view))
{var e_viewpoint=viewarea._scene.getViewpoint();var e_eventType="viewpointChanged";try{if(e_viewpoint._xmlNode&&(e_viewpoint._xmlNode["on"+e_eventType]||e_viewpoint._xmlNode.hasAttribute("on"+e_eventType)||e_viewpoint._listeners[e_eventType]))
{var e_viewtrafo=e_viewpoint.getCurrentTransform();e_viewtrafo=e_viewtrafo.inverse().mult(mat_view);var e_mat=e_viewtrafo.inverse();var e_rotation=new x3dom.fields.Quaternion(0,0,1,0);e_rotation.setValue(e_mat);var e_translation=e_mat.e3();var e_event={target:e_viewpoint._xmlNode,type:e_eventType,matrix:e_viewtrafo,position:e_translation,orientation:e_rotation.toAxisAngle(),cancelBubble:false,stopPropagation:function(){this.cancelBubble=true;}};e_viewpoint.callEvtHandler(e_eventType,e_event);}}
catch(e_e){x3dom.debug.logException(e_e);}}
viewarea._last_mat_view=mat_view;var viewpoint=scene.getViewpoint();viewpoint._vf.zFar=100000;viewpoint._vf.zNear=0.01;var mat_proj=viewarea.getProjectionMatrix();this.object.setViewpoint({fov:viewpoint._vf.fov,zFar:viewpoint._vf.zFar,zNear:viewpoint._vf.zNear,viewMatrix:mat_view.toGL(),projectionMatrix:mat_proj.toGL()});var nav=scene.getNavigationInfo();if(nav._vf.headlight){this.object.setLights({idx:0,type:0,on:1.0,color:[1.0,1.0,1.0],intensity:1.0,ambientIntensity:0.0,direction:[0.0,0.0,1.0],attenuation:[1.0,1.0,1.0],location:[1.0,1.0,1.0],radius:0.0,beamWidth:0.0,cutOffAngle:0.0});}
var lights=viewarea.getLights();for(var i=0;i<lights.length;i++){if(lights[i]._dirty){if(x3dom.isa(lights[i],x3dom.nodeTypes.DirectionalLight))
{}
else if(x3dom.isa(lights[i],x3dom.nodeTypes.PointLight))
{}
else if(x3dom.isa(lights[i],x3dom.nodeTypes.SpotLight))
{}
lights[i]._dirty=false;}}};Context.prototype.setupBackground=function(background){if(background._dirty)
{this.object.setBackground({texURLs:background.getTexUrl(),skyAngle:background._vf.skyAngle,skyColor:background.getSkyColor().toGL(),groundAngle:background._vf.groundAngle,groundColor:background.getGroundColor().toGL(),transparency:background.getTransparency()});background._dirty=false;}};Context.prototype.setupShape=function(shape,trafo,refID){if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.PointSet)){x3dom.debug.logError("Flash backend don't support PointSets yet");return;}else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.IndexedLineSet)){x3dom.debug.logError("Flash backend don't support LineSets yet");return;}else if(x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.Text)){this.setupText(shape,trafo,refID);}else{this.setupIndexedFaceSet(shape,trafo,refID);}};Context.prototype.setupIndexedFaceSet=function(shape,trafo,refID)
{this.object.setMeshTransform({id:shape._objectID,refID:refID,transform:trafo.toGL()});if(refID==0)
{var isImageGeometry=x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.ImageGeometry);var isBinaryGeometry=x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BinaryGeometry);var isBitLODGeometry=x3dom.isa(shape._cf.geometry.node,x3dom.nodeTypes.BitLODGeometry);var appearance=shape._cf.appearance.node;var sortType=(appearance)?shape._cf.appearance.node._vf.sortType:"auto";var sortKey=(appearance)?shape._cf.appearance.node._vf.sortKey:0
if(isImageGeometry){this.object.setMeshProperties({id:shape._objectID,type:"ImageGeometry",sortType:sortType,sortKey:sortKey,solid:shape.isSolid(),bboxMin:shape._cf.geometry.node.getMin().toGL(),bboxMax:shape._cf.geometry.node.getMax().toGL(),bboxCenter:shape._cf.geometry.node.getCenter().toGL(),primType:shape._cf.geometry.node._vf.primType,vertexCount:shape._cf.geometry.node._vf.vertexCount});}else if(isBinaryGeometry){this.object.setMeshProperties({id:shape._objectID,type:"BinaryGeometry",sortType:sortType,sortKey:sortKey,solid:shape.isSolid(),bgCenter:shape._cf.geometry.node._vf.position.toGL(),bgSize:shape._cf.geometry.node._vf.size.toGL(),bboxCenter:shape._cf.geometry.node.getCenter().toGL(),primType:shape._cf.geometry.node._vf.primType,vertexCount:shape._cf.geometry.node._vf.vertexCount});}else if(isBitLODGeometry){this.object.setMeshProperties({id:shape._objectID,type:"BitLODGeometry",sortType:sortType,sortKey:sortKey,solid:shape.isSolid(),bboxMin:shape._cf.geometry.node.getMin().toGL(),bboxMax:shape._cf.geometry.node.getMax().toGL(),bboxCenter:shape._cf.geometry.node.getCenter().toGL(),primType:shape._cf.geometry.node._vf.primType,vertexCount:shape._cf.geometry.node._vf.vertexCount});}else{this.object.setMeshProperties({id:shape._objectID,type:"Default",sortType:sortType,sortKey:sortKey,solid:shape.isSolid()});}
if(shape._dirty.indexes===true){if(isImageGeometry){}else if(isBinaryGeometry){this.object.setMeshIndices({id:shape._objectID,idx:0,indices:shape._nameSpace.getURL(shape._cf.geometry.node._vf.index)});}else if(isBitLODGeometry){this.object.setMeshIndices({id:shape._objectID,idx:0,indices:shape._nameSpace.getURL(shape._cf.geometry.node._vf.index)});}else{for(var i=0;i<shape._cf.geometry.node._mesh._indices.length;i++){this.object.setMeshIndices({id:shape._objectID,idx:i,indices:shape._cf.geometry.node._mesh._indices[i]});}}
shape._dirty.indexes=false;}
if(shape._dirty.positions===true){if(isImageGeometry){this.object.setMeshVertices({id:shape._objectID,idx:0,coordinateTexture0:shape._cf.geometry.node.getCoordinateTextureURL(0),coordinateTexture1:shape._cf.geometry.node.getCoordinateTextureURL(1)});}else if(isBinaryGeometry){this.object.setMeshVertices({id:shape._objectID,idx:0,interleaved:shape._cf.geometry.node._hasStrideOffset,vertices:shape._nameSpace.getURL(shape._cf.geometry.node._vf.coord),normals:shape._nameSpace.getURL(shape._cf.geometry.node._vf.normal),texCoords:shape._nameSpace.getURL(shape._cf.geometry.node._vf.texCoord),colors:shape._nameSpace.getURL(shape._cf.geometry.node._vf.color),numColorComponents:shape._cf.geometry.node._mesh._numColComponents,numNormalComponents:shape._cf.geometry.node._mesh._numNormComponents,vertexType:shape._cf.geometry.node._vf.coordType,normalType:shape._cf.geometry.node._vf.normalType,texCoordType:shape._cf.geometry.node._vf.texCoordType,colorType:shape._cf.geometry.node._vf.colorType,vertexStrideOffset:shape._coordStrideOffset,normalStrideOffset:shape._normalStrideOffset,texCoordStrideOffset:shape._texCoordStrideOffset,colorStrideOffset:shape._colorStrideOffset});}else if(isBitLODGeometry){this.object.setMeshVertices({id:shape._objectID,componentURLs:shape._cf.geometry.node.getComponentsURLs(),componentFormats:shape._cf.geometry.node.getComponentFormats(),componentAttribs:shape._cf.geometry.node.getComponentAttribs()});}else{for(var i=0;i<shape._cf.geometry.node._mesh._positions.length;i++){this.object.setMeshVertices({id:shape._objectID,idx:i,vertices:shape._cf.geometry.node._mesh._positions[i]});}}
shape._dirty.positions=false;}
if(shape._dirty.normals===true){if(isImageGeometry){this.object.setMeshNormals({id:shape._objectID,idx:0,normalTexture:shape._cf.geometry.node.getNormalTextureURL()});}else if(isBinaryGeometry){if(!shape._cf.geometry.node._hasStrideOffset){this.object.setMeshNormals({id:shape._objectID,idx:0,normals:shape._nameSpace.getURL(shape._cf.geometry.node._vf.normal)});}}else if(isBitLODGeometry){}else{if(shape._cf.geometry.node._mesh._normals[0].length){for(var i=0;i<shape._cf.geometry.node._mesh._normals.length;i++){this.object.setMeshNormals({id:shape._objectID,idx:i,normals:shape._cf.geometry.node._mesh._normals[i]});}}}
shape._dirty.normals=false;}
if(shape._dirty.colors===true){if(isImageGeometry){this.object.setMeshColors({id:shape._objectID,idx:0,colorTexture:shape._cf.geometry.node.getColorTextureURL(),components:shape._cf.geometry.node._mesh._numColComponents});}else if(isBinaryGeometry){if(!shape._cf.geometry.node._hasStrideOffset){this.object.setMeshColors({id:shape._objectID,idx:0,colors:shape._nameSpace.getURL(shape._cf.geometry.node._vf.color),components:shape._cf.geometry.node._mesh._numColComponents});}}else if(isBitLODGeometry){}else{if(shape._cf.geometry.node._mesh._colors[0].length){for(var i=0;i<shape._cf.geometry.node._mesh._colors.length;i++){this.object.setMeshColors({id:shape._objectID,idx:i,colors:shape._cf.geometry.node._mesh._colors[i],components:shape._cf.geometry.node._mesh._numColComponents});}}}
shape._dirty.colors=false;}
if(shape._dirty.texcoords===true){if(isImageGeometry){this.object.setMeshTexCoords({id:shape._objectID,idx:0,texCoordTexture:shape._cf.geometry.node.getTexCoordTextureURL()});}else if(isBinaryGeometry){if(!shape._cf.geometry.node._hasStrideOffset){this.object.setMeshTexCoords({id:shape._objectID,idx:0,texCoords:shape._nameSpace.getURL(shape._cf.geometry.node._vf.texCoord)});}}else if(isBitLODGeometry){}else{if(shape._cf.geometry.node._mesh._texCoords[0].length){for(var i=0;i<shape._cf.geometry.node._mesh._texCoords.length;i++){this.object.setMeshTexCoords({id:shape._objectID,idx:i,texCoords:shape._cf.geometry.node._mesh._texCoords[i]});}}}
shape._dirty.texcoords=false;}
if(shape._dirty.material===true){if(appearance){var material=shape._cf.appearance.node._cf.material.node;if(material){this.object.setMeshMaterial({id:shape._objectID,ambientIntensity:material._vf.ambientIntensity,diffuseColor:material._vf.diffuseColor.toGL(),emissiveColor:material._vf.emissiveColor.toGL(),shininess:material._vf.shininess,specularColor:material._vf.specularColor.toGL(),transparency:material._vf.transparency});}}
shape._dirty.material=false;}
if(shape._dirty.texture===true){if(appearance){var texture=shape._cf.appearance.node._cf.texture.node;if(texture){if(x3dom.isa(texture,x3dom.nodeTypes.PixelTexture))
{this.object.setPixelTexture({id:shape._objectID,width:texture._vf.image.width,height:texture._vf.image.height,comp:texture._vf.image.comp,pixels:texture._vf.image.toGL()});}else if(x3dom.isa(texture,x3dom.nodeTypes.ComposedCubeMapTexture)){this.object.setCubeTexture({id:shape._objectID,texURLs:texture.getTexUrl()});}else if(texture._isCanvas&&texture._canvas){this.object.setCanvasTexture({id:shape._objectID,width:texture._canvas.width,height:texture._canvas.height,dataURL:texture._canvas.toDataURL()});}else if(x3dom.isa(texture,x3dom.nodeTypes.MultiTexture)){x3dom.debug.logError("Flash backend don't support MultiTextures yet");}else if(x3dom.isa(texture,x3dom.nodeTypes.MovieTexture)){x3dom.debug.logError("Flash backend don't support MovieTextures yet");}else{this.object.setMeshTexture({id:shape._objectID,origChannelCount:texture._vf.origChannelCount,repeatS:texture._vf.repeatS,repeatT:texture._vf.repeatT,url:texture._vf.url[0]});}}}
shape._dirty.texture=false;}
if(shape._cf.geometry.node._cf.texCoord!==undefined&&shape._cf.geometry.node._cf.texCoord.node!==null&&!x3dom.isa(shape._cf.geometry.node._cf.texCoord.node,x3dom.nodeTypes.X3DTextureNode)&&shape._cf.geometry.node._cf.texCoord.node._vf.mode)
{var texMode=shape._cf.geometry.node._cf.texCoord.node._vf.mode;if(texMode.toLowerCase()=="sphere"){this.object.setSphereMapping({id:shape._objectID,sphereMapping:1});}
else{this.object.setSphereMapping({id:shape._objectID,sphereMapping:0});}}
else{this.object.setSphereMapping({id:shape._objectID,sphereMapping:0});}}};Context.prototype.setupText=function(shape,trafo,refID)
{this.object.setMeshTransform({id:shape._objectID,refID:refID,transform:trafo.toGL()});if(refID==0)
{var appearance=shape._cf.appearance.node;var sortType=(appearance)?shape._cf.appearance.node._vf.sortType:"auto";var sortKey=(appearance)?shape._cf.appearance.node._vf.sortKey:0
if(shape._dirty.text===true){var fontStyleNode=shape._cf.geometry.node._cf.fontStyle.node;if(fontStyleNode===null){this.object.setMeshProperties({id:shape._objectID,type:"Text",sortType:sortType,sortKey:sortKey,solid:shape.isSolid(),text:shape._cf.geometry.node._vf.string,fontFamily:['SERIF'],fontStyle:"PLAIN",fontAlign:"BEGIN",fontSize:32,fontSpacing:1.0,fontHorizontal:true,fontLanguage:"",fontLeftToRight:true,fontTopToBottom:true});}else{this.object.setMeshProperties({id:shape._objectID,type:"Text",sortType:sortType,sortKey:sortKey,solid:shape.isSolid(),text:shape._cf.geometry.node._vf.string,fontFamily:fontStyleNode._vf.family.toString(),fontStyle:fontStyleNode._vf.style.toString(),fontAlign:fontStyleNode._vf.justify.toString(),fontSize:fontStyleNode._vf.size,fontSpacing:fontStyleNode._vf.spacing,fontHorizontal:fontStyleNode._vf.horizontal,fontLanguage:fontStyleNode._vf.language,fontLeftToRight:fontStyleNode._vf.leftToRight,fontTopToBottom:fontStyleNode._vf.topToBottom});}
shape._dirty.text=false;}
if(shape._dirty.material===true){if(appearance){var material=shape._cf.appearance.node._cf.material.node;if(material){this.object.setMeshMaterial({id:shape._objectID,ambientIntensity:material._vf.ambientIntensity,diffuseColor:material._vf.diffuseColor.toGL(),emissiveColor:material._vf.emissiveColor.toGL(),shininess:material._vf.shininess,specularColor:material._vf.specularColor.toGL(),transparency:material._vf.transparency});}}
shape._dirty.material=false;}}};Context.prototype.pickValue=function(viewarea,x,y,viewMat,sceneMat)
{var scene=viewarea._scene;if(this.object===null||scene===null||scene.drawableObjects===undefined||!scene.drawableObjects||scene._vf.pickMode.toLowerCase()==="box")
{return false;}
var pickMode=(scene._vf.pickMode.toLowerCase()==="color")?1:((scene._vf.pickMode.toLowerCase()==="texcoord")?2:0);var data=this.object.pickValue({pickMode:pickMode});if(data.objID>0){viewarea._pickingInfo.pickPos=new x3dom.fields.SFVec3f(data.pickPosX,data.pickPosY,data.pickPosZ);viewarea._pickingInfo.pickObj=x3dom.nodeTypes.Shape.idMap.nodeID[data.objID];}else{viewarea._pickingInfo.pickObj=null;viewarea._pickingInfo.lastClickObj=null;}
return true;};Context.prototype.shutdown=function(viewarea)
{};return setupContext;})();x3dom.X3DDocument=function(canvas,ctx,settings){this.properties=settings;this.canvas=canvas;this.ctx=ctx;this.needRender=true;this._scene=null;this._viewarea=null;this._nodeBag={timer:[],lights:[],clipPlanes:[],followers:[],trans:[],renderTextures:[],viewarea:[]};this.downloadCount=0;this.onload=function(){};this.onerror=function(){};};x3dom.X3DDocument.prototype.load=function(uri,sceneElemPos){var uri_docs={};var queued_uris=[uri];var doc=this;function next_step(){if(queued_uris.length===0){doc._setup(uri_docs[uri],uri_docs,sceneElemPos);doc.onload();return;}
var next_uri=queued_uris.shift();if(x3dom.isX3DElement(next_uri)&&(next_uri.localName.toLowerCase()==='x3d'||next_uri.localName.toLowerCase()==='websg'))
{uri_docs[next_uri]=next_uri;next_step();}}
next_step();};x3dom.findScene=function(x3dElem){var sceneElems=[];for(var i=0;i<x3dElem.childNodes.length;i++){var sceneElem=x3dElem.childNodes[i];if(sceneElem&&sceneElem.localName&&sceneElem.localName.toLowerCase()==="scene"){sceneElems.push(sceneElem);}}
if(sceneElems.length>1){x3dom.debug.logError("X3D element has more than one Scene child (has "+
x3dElem.childNodes.length+").");}
else{return sceneElems[0];}
return null;};x3dom.X3DDocument.prototype._setup=function(sceneDoc,uriDocs,sceneElemPos){var doc=this;var domEventListener={onAttrModified:function(e){if('_x3domNode'in e.target){var attrToString={1:"MODIFICATION",2:"ADDITION",3:"REMOVAL"};e.target._x3domNode.updateField(e.attrName,e.newValue);doc.needRender=true;}},onNodeRemoved:function(e){if('_x3domNode'in e.target.parentNode&&'_x3domNode'in e.target){var parent=e.target.parentNode._x3domNode;var child=e.target._x3domNode;if(parent&&child){parent.removeChild(child);if(doc._viewarea&&doc._viewarea._scene)
doc._viewarea._scene.updateVolume();doc.needRender=true;}}
else if(e.target.localName&&e.target.localName.toUpperCase()=="ROUTE"){x3dom.debug.logError("Remove ROUTE NYI");}},onNodeInserted:function(e){var child=e.target;if('_x3domNode'in child.parentNode){if(child.parentNode.tagName&&child.parentNode.tagName.toLowerCase()=='inline'){return;}
else{var parent=child.parentNode._x3domNode;if(parent&&parent._nameSpace){var newNode=parent._nameSpace.setupTree(child);if(child instanceof Element){parent.addChild(newNode,child.getAttribute("containerField"));if(doc._viewarea&&doc._viewarea._scene)
doc._viewarea._scene.updateVolume();}
else
parent.nodeChanged();doc.needRender=true;}
else{x3dom.debug.logWarning("No _nameSpace in onNodeInserted");}}}}};sceneDoc.addEventListener('DOMNodeRemoved',domEventListener.onNodeRemoved,true);sceneDoc.addEventListener('DOMNodeInserted',domEventListener.onNodeInserted,true);if((x3dom.userAgentFeature.supportsDOMAttrModified===true)){sceneDoc.addEventListener('DOMAttrModified',domEventListener.onAttrModified,true);}
var sceneElem=x3dom.findScene(sceneDoc);this._bindableBag=new x3dom.BindableBag(this);var nameSpace=new x3dom.NodeNameSpace("scene",doc);var scene=nameSpace.setupTree(sceneElem);this._scene=scene;this._bindableBag.setRefNode(scene);this._viewarea=new x3dom.Viewarea(this,scene);this._viewarea._width=this.canvas.width;this._viewarea._height=this.canvas.height;};x3dom.X3DDocument.prototype.advanceTime=function(t){var i=0;if(this._nodeBag.timer.length){for(i=0;i<this._nodeBag.timer.length;i++)
{this.needRender|=this._nodeBag.timer[i].tick(t);}}
if(this._nodeBag.followers.length){for(i=0;i<this._nodeBag.followers.length;i++)
{this.needRender|=this._nodeBag.followers[i].tick(t);}}
if(this._nodeBag.trans.length){for(i=0;i<this._nodeBag.trans.length;i++)
{this.needRender|=this._nodeBag.trans[i].tick(t);}}
if(this._nodeBag.viewarea.length){for(i=0;i<this._nodeBag.viewarea.length;i++)
{this.needRender|=this._nodeBag.viewarea[i].tick(t);}}};x3dom.X3DDocument.prototype.render=function(ctx){if(!ctx||!this._viewarea){return;}
ctx.renderScene(this._viewarea);};x3dom.X3DDocument.prototype.onPick=function(ctx,x,y){if(!ctx||!this._viewarea){return;}
ctx.pickValue(this._viewarea,x,y,1);};x3dom.X3DDocument.prototype.onPickRect=function(ctx,x1,y1,x2,y2){if(!ctx||!this._viewarea){return;}
return ctx.pickRect(this._viewarea,x1,y1,x2,y2);};x3dom.X3DDocument.prototype.onMove=function(ctx,x,y,buttonState){if(!ctx||!this._viewarea){return;}
if(this._viewarea._scene._vf.doPickPass)
ctx.pickValue(this._viewarea,x,y,buttonState);this._viewarea.onMove(x,y,buttonState);};x3dom.X3DDocument.prototype.onMoveView=function(ctx,translation,rotation){if(!ctx||!this._viewarea){return;}
this._viewarea.onMoveView(translation,rotation);};x3dom.X3DDocument.prototype.onDrag=function(ctx,x,y,buttonState){if(!ctx||!this._viewarea){return;}
if(this._viewarea._scene._vf.doPickPass)
ctx.pickValue(this._viewarea,x,y,buttonState);this._viewarea.onDrag(x,y,buttonState);};x3dom.X3DDocument.prototype.onMousePress=function(ctx,x,y,buttonState){if(!ctx||!this._viewarea){return;}
this._viewarea._scene.updateVolume();ctx.pickValue(this._viewarea,x,y,buttonState);this._viewarea.onMousePress(x,y,buttonState);};x3dom.X3DDocument.prototype.onMouseRelease=function(ctx,x,y,buttonState){if(!ctx||!this._viewarea){return;}
ctx.pickValue(this._viewarea,x,y,buttonState);this._viewarea.onMouseRelease(x,y,buttonState);};x3dom.X3DDocument.prototype.onMouseOver=function(ctx,x,y,buttonState){if(!ctx||!this._viewarea){return;}
ctx.pickValue(this._viewarea,x,y,buttonState);this._viewarea.onMouseOver(x,y,buttonState);};x3dom.X3DDocument.prototype.onMouseOut=function(ctx,x,y,buttonState){if(!ctx||!this._viewarea){return;}
ctx.pickValue(this._viewarea,x,y,buttonState);this._viewarea.onMouseOut(x,y,buttonState);};x3dom.X3DDocument.prototype.onDoubleClick=function(ctx,x,y){if(!ctx||!this._viewarea){return;}
this._viewarea.onDoubleClick(x,y);};x3dom.X3DDocument.prototype.onKeyDown=function(keyCode)
{switch(keyCode){case 37:this._viewarea.strafeLeft();break;case 38:this._viewarea.moveFwd();break;case 39:this._viewarea.strafeRight();break;case 40:this._viewarea.moveBwd();break;default:}};x3dom.X3DDocument.prototype.onKeyUp=function(keyCode)
{var stack=null;switch(keyCode){case 27:window.history.back();break;case 33:stack=this._scene.getViewpoint()._stack;if(stack){stack.switchTo('next');}
else{x3dom.debug.logError('No valid ViewBindable stack.');}
break;case 34:stack=this._scene.getViewpoint()._stack;if(stack){stack.switchTo('prev');}
else{x3dom.debug.logError('No valid ViewBindable stack.');}
break;case 37:break;case 38:break;case 39:break;case 40:break;default:}};x3dom.X3DDocument.prototype.onKeyPress=function(charCode)
{var nav=this._scene.getNavigationInfo();switch(charCode)
{case 32:var states=this.canvas.parent.stateViewer;if(states){states.display();}
x3dom.debug.logInfo("a: show all | d: show helper buffers | s: light view | "+"m: toggle render mode | p: intersect type | r: reset view | "+"e: examine mode | f: fly mode | w: walk mode | h: helicopter mode | "+"l: lookAt mode | g: game mode | u: upright position");break;case 43:nav._vf.speed=2*nav._vf.speed;x3dom.debug.logInfo("Changed navigation speed to "+nav._vf.speed);break;case 45:nav._vf.speed=0.5*nav._vf.speed;x3dom.debug.logInfo("Changed navigation speed to "+nav._vf.speed);break;case 51:x3dom.nodeTypes.PopGeometry.ErrorToleranceFactor+=0.5;x3dom.debug.logInfo("Changed POP error tolerance to "+x3dom.nodeTypes.PopGeometry.ErrorToleranceFactor);break;case 52:x3dom.nodeTypes.PopGeometry.ErrorToleranceFactor-=0.5;x3dom.debug.logInfo("Changed POP error tolerance to "+x3dom.nodeTypes.PopGeometry.ErrorToleranceFactor);break;case 54:nav._vf.typeParams[1]+=1.0;nav._heliUpdated=false;x3dom.debug.logInfo("Changed helicopter height to "+nav._vf.typeParams[1]);break;case 55:nav._vf.typeParams[1]-=1.0;nav._heliUpdated=false;x3dom.debug.logInfo("Changed helicopter height to "+nav._vf.typeParams[1]);break;case 56:nav._vf.typeParams[0]-=0.02;nav._heliUpdated=false;x3dom.debug.logInfo("Changed helicopter angle to "+nav._vf.typeParams[0]);break;case 57:nav._vf.typeParams[0]+=0.02;nav._heliUpdated=false;x3dom.debug.logInfo("Changed helicopter angle to "+nav._vf.typeParams[0]);break;case 97:this._viewarea.showAll();break;case 100:if(this._viewarea._visDbgBuf===undefined){this._viewarea._visDbgBuf=true;}
else{this._viewarea._visDbgBuf=!this._viewarea._visDbgBuf;}
x3dom.debug.logContainer.style.display=(this._viewarea._visDbgBuf===true)?"block":"none";break;case 101:nav.setType("examine",this._viewarea);break;case 102:nav.setType("fly",this._viewarea);break;case 103:nav.setType("game",this._viewarea);break;case 104:nav.setType("helicopter",this._viewarea);break;case 108:nav.setType("lookat",this._viewarea);break;case 109:if(this._viewarea._points===undefined){this._viewarea._points=0;}
this._viewarea._points=++this._viewarea._points%3;break;case 111:nav.setType("lookaround",this._viewarea);break;case 112:switch(this._scene._vf.pickMode.toLowerCase())
{case"idbuf":this._scene._vf.pickMode="color";break;case"color":this._scene._vf.pickMode="texCoord";break;case"texcoord":this._scene._vf.pickMode="box";break;default:this._scene._vf.pickMode="idBuf";break;}
x3dom.debug.logInfo("Switch pickMode to '"+
this._scene._vf.pickMode+"'.");break;case 114:this._viewarea.resetView();break;case 115:if(this._nodeBag.lights.length>0)
{this._viewarea.animateTo(this._viewarea.getLightMatrix()[0],this._scene.getViewpoint());}
break;case 117:this._viewarea.uprightView();break;case 118:var that=this;(function(){var mat_view=that._viewarea.getViewMatrix();var e_viewpoint=that._viewarea._scene.getViewpoint();var e_viewtrafo=e_viewpoint.getCurrentTransform();e_viewtrafo=e_viewtrafo.inverse().mult(mat_view);var e_mat=e_viewtrafo.inverse();var rotation=new x3dom.fields.Quaternion(0,0,1,0);rotation.setValue(e_mat);var translation=e_mat.e3();var rot=rotation.toAxisAngle();x3dom.debug.logInfo('&lt;Viewpoint position="'+translation.x.toFixed(5)+' '
+translation.y.toFixed(5)+' '+translation.z.toFixed(5)+'" '+'orientation="'+rot[0].x.toFixed(5)+' '+rot[0].y.toFixed(5)+' '
+rot[0].z.toFixed(5)+' '+rot[1].toFixed(5)+'" \n\t'+'zNear="'+e_viewpoint.getNear().toFixed(6)+'" '+'zFar="'+e_viewpoint.getFar().toFixed(6)+'" '+'description="'+e_viewpoint._vf.description+'"&gt;');})();break;case 119:nav.setType("walk",this._viewarea);break;default:}};x3dom.X3DDocument.prototype.shutdown=function(ctx)
{if(!ctx){return;}
ctx.shutdown(this._viewarea);};x3dom.MatrixMixer=function(beginTime,endTime){if(arguments.length===0){this._beginTime=0;this._endTime=1;}
else{this._beginTime=beginTime;this._endTime=endTime;}
this._beginMat=x3dom.fields.SFMatrix4f.identity();this._beginInvMat=x3dom.fields.SFMatrix4f.identity();this._beginLogMat=x3dom.fields.SFMatrix4f.identity();this._endMat=x3dom.fields.SFMatrix4f.identity();this._endLogMat=x3dom.fields.SFMatrix4f.identity();};x3dom.MatrixMixer.prototype.calcFraction=function(time){var fraction=(time-this._beginTime)/(this._endTime-this._beginTime);return(Math.sin((fraction*Math.PI)-(Math.PI/2))+1)/2.0;};x3dom.MatrixMixer.prototype.setBeginMatrix=function(mat){this._beginMat.setValues(mat);this._beginInvMat=mat.inverse();this._beginLogMat=x3dom.fields.SFMatrix4f.zeroMatrix();};x3dom.MatrixMixer.prototype.setEndMatrix=function(mat){this._endMat.setValues(mat);this._endLogMat=mat.mult(this._beginInvMat).log();this._logDiffMat=this._endLogMat.addScaled(this._beginLogMat,-1);};x3dom.MatrixMixer.prototype.mix=function(time){var mat=null;if(time<=this._beginTime)
{mat=x3dom.fields.SFMatrix4f.copy(this._beginLogMat);}
else
{if(time>=this._endTime)
{mat=x3dom.fields.SFMatrix4f.copy(this._endLogMat);}
else
{var fraction=this.calcFraction(time);mat=this._logDiffMat.multiply(fraction).add(this._beginLogMat);}}
return mat.exp().mult(this._beginMat);};x3dom.Viewarea=function(document,scene){this._doc=document;this._scene=scene;document._nodeBag.viewarea.push(this);this._pickingInfo={pickPos:new x3dom.fields.SFVec3f(0,0,0),pickNorm:new x3dom.fields.SFVec3f(0,0,1),pickObj:null,lastObj:null,lastClickObj:null};this._rotMat=x3dom.fields.SFMatrix4f.identity();this._transMat=x3dom.fields.SFMatrix4f.identity();this._movement=new x3dom.fields.SFVec3f(0,0,0);this._relMat=x3dom.fields.SFMatrix4f.identity();this._needNavigationMatrixUpdate=true;this._deltaT=0;this._pitch=0;this._yaw=0;this._eyePos=new x3dom.fields.SFVec3f(0,0,0);this._width=400;this._height=300;this._dx=0;this._dy=0;this._lastX=-1;this._lastY=-1;this._pressX=-1;this._pressY=-1;this._lastButton=0;this._hasTouches=false;this._numRenderedNodes=0;this._pick=new x3dom.fields.SFVec3f(0,0,0);this._pickNorm=new x3dom.fields.SFVec3f(0,0,1);this._isAnimating=false;this._lastTS=0;this._mixer=new x3dom.MatrixMixer();};x3dom.Viewarea.prototype.tick=function(timeStamp)
{var needMixAnim=false;if(this._mixer._beginTime>0)
{needMixAnim=true;if(timeStamp>=this._mixer._beginTime)
{if(timeStamp<=this._mixer._endTime)
{var mat=this._mixer.mix(timeStamp);this._scene.getViewpoint().setView(mat);}
else{this._mixer._beginTime=0;this._mixer._endTime=0;this._scene.getViewpoint().setView(this._mixer._endMat);}}
else{this._mixer._beginTime=0;this._mixer._endTime=0;this._scene.getViewpoint().setView(this._mixer._beginMat);}}
var needNavAnim=this.navigateTo(timeStamp);var lastIsAnimating=this._isAnimating;this._lastTS=timeStamp;this._isAnimating=(needMixAnim||needNavAnim);return(this._isAnimating||lastIsAnimating);};x3dom.Viewarea.prototype.isMoving=function()
{return(this._lastButton>0||this._hasTouches||this._isAnimating);};x3dom.Viewarea.prototype.navigateTo=function(timeStamp)
{var navi=this._scene.getNavigationInfo();var needNavAnim=(navi._vf.type[0].toLowerCase()==="game"||(this._lastButton>0&&(navi._vf.type[0].toLowerCase()==="fly"||navi._vf.type[0].toLowerCase()==="walk"||navi._vf.type[0].toLowerCase()==="helicopter"||navi._vf.type[0].toLowerCase().substr(0,5)==="looka")));this._deltaT=timeStamp-this._lastTS;if(needNavAnim)
{var avatarRadius=0.25;var avatarHeight=1.6;var avatarKnee=0.75;if(navi._vf.avatarSize.length>2){avatarRadius=navi._vf.avatarSize[0];avatarHeight=navi._vf.avatarSize[1];avatarKnee=navi._vf.avatarSize[2];}
var currViewMat=this.getViewMatrix();var dist=0;var step=(this._lastButton&2)?-1:1;step*=(this._deltaT*navi._vf.speed);var phi=Math.PI*this._deltaT*(this._pressX-this._lastX)/this._width;var theta=Math.PI*this._deltaT*(this._pressY-this._lastY)/this._height;if(this._needNavigationMatrixUpdate===true)
{this._needNavigationMatrixUpdate=false;this._rotMat=x3dom.fields.SFMatrix4f.identity();this._transMat=x3dom.fields.SFMatrix4f.identity();this._movement=new x3dom.fields.SFVec3f(0,0,0);this._relMat=x3dom.fields.SFMatrix4f.identity();var angleX=0;var angleY=Math.asin(currViewMat._02);var C=Math.cos(angleY);if(Math.abs(C)>0.0001){angleX=Math.atan2(-currViewMat._12/C,currViewMat._22/C);}
this._flyMat=currViewMat.inverse();this._from=this._flyMat.e3();this._at=this._from.subtract(this._flyMat.e2());if(navi._vf.type[0].toLowerCase()==="helicopter")
this._at.y=this._from.y;if(navi._vf.type[0].toLowerCase().substr(0,5)!=="looka")
this._up=new x3dom.fields.SFVec3f(0,1,0);else
this._up=this._flyMat.e1();this._pitch=angleX*180/Math.PI;this._yaw=angleY*180/Math.PI;this._eyePos=this._from.negate();}
var tmpAt=null,tmpUp=null,tmpMat=null;var q,temp,fin;var lv,sv,up;if(navi._vf.type[0].toLowerCase()==="game")
{this._pitch+=this._dy;this._yaw+=this._dx;if(this._pitch>=89)this._pitch=89;if(this._pitch<=-89)this._pitch=-89;if(this._yaw>=360)this._yaw-=360;if(this._yaw<0)this._yaw=360+this._yaw;this._dx=0;this._dy=0;var xMat=x3dom.fields.SFMatrix4f.rotationX(this._pitch/180*Math.PI);var yMat=x3dom.fields.SFMatrix4f.rotationY(this._yaw/180*Math.PI);var fPos=x3dom.fields.SFMatrix4f.translation(this._eyePos);this._flyMat=xMat.mult(yMat).mult(fPos);var flyMat=this._flyMat.inverse();var tmpFrom=flyMat.e3();tmpUp=new x3dom.fields.SFVec3f(0,-1,0);tmpAt=tmpFrom.add(tmpUp);tmpUp=flyMat.e0().cross(tmpUp).normalize();tmpMat=x3dom.fields.SFMatrix4f.lookAt(tmpFrom,tmpAt,tmpUp);tmpMat=tmpMat.inverse();this._scene._nameSpace.doc.ctx.pickValue(this,this._width/2,this._height/2,this._lastButton,tmpMat,this.getProjectionMatrix().mult(tmpMat));if(this._pickingInfo.pickObj)
{dist=this._pickingInfo.pickPos.subtract(tmpFrom).length();tmpFrom.y+=(avatarHeight-dist);flyMat.setTranslate(tmpFrom);this._eyePos=flyMat.e3().negate();this._flyMat=flyMat.inverse();this._pickingInfo.pickObj=null;}
this._scene.getViewpoint().setView(this._flyMat);return needNavAnim;}
else if(navi._vf.type[0].toLowerCase()==="helicopter")
{var typeParams=navi.getTypeParams();if(this._lastButton&2)
{var stepUp=this._deltaT*this._deltaT*navi._vf.speed;stepUp*=0.1*(this._pressY-this._lastY)*Math.abs(this._pressY-this._lastY);typeParams[1]+=stepUp;navi.setTypeParams(typeParams);}
if(this._lastButton&1){step*=this._deltaT*(this._pressY-this._lastY)*Math.abs(this._pressY-this._lastY);}
else{step=0;}
theta=typeParams[0];this._from.y=typeParams[1];this._at.y=this._from.y;q=x3dom.fields.Quaternion.axisAngle(this._up,phi);temp=q.toMatrix();fin=x3dom.fields.SFMatrix4f.translation(this._from);fin=fin.mult(temp);temp=x3dom.fields.SFMatrix4f.translation(this._from.negate());fin=fin.mult(temp);this._at=fin.multMatrixPnt(this._at);lv=this._at.subtract(this._from).normalize();sv=lv.cross(this._up).normalize();up=sv.cross(lv).normalize();lv=lv.multiply(step);this._from=this._from.add(lv);this._at=this._at.add(lv);q=x3dom.fields.Quaternion.axisAngle(sv,theta);temp=q.toMatrix();fin=x3dom.fields.SFMatrix4f.translation(this._from);fin=fin.mult(temp);temp=x3dom.fields.SFMatrix4f.translation(this._from.negate());fin=fin.mult(temp);var at=fin.multMatrixPnt(this._at);this._flyMat=x3dom.fields.SFMatrix4f.lookAt(this._from,at,up);this._scene.getViewpoint().setView(this._flyMat.inverse());return needNavAnim;}
q=x3dom.fields.Quaternion.axisAngle(this._up,phi);temp=q.toMatrix();fin=x3dom.fields.SFMatrix4f.translation(this._from);fin=fin.mult(temp);temp=x3dom.fields.SFMatrix4f.translation(this._from.negate());fin=fin.mult(temp);this._at=fin.multMatrixPnt(this._at);lv=this._at.subtract(this._from).normalize();sv=lv.cross(this._up).normalize();up=sv.cross(lv).normalize();q=x3dom.fields.Quaternion.axisAngle(sv,theta);temp=q.toMatrix();fin=x3dom.fields.SFMatrix4f.translation(this._from);fin=fin.mult(temp);temp=x3dom.fields.SFMatrix4f.translation(this._from.negate());fin=fin.mult(temp);this._at=fin.multMatrixPnt(this._at);if(navi._vf.type[0].toLowerCase().substr(0,5)!=="looka")
{var currProjMat=this.getProjectionMatrix();if(step<0){tmpMat=new x3dom.fields.SFMatrix4f();tmpMat.setValue(this._last_mat_view.e0(),this._last_mat_view.e1(),this._last_mat_view.e2().negate(),this._last_mat_view.e3());this._scene._nameSpace.doc.ctx.pickValue(this,this._width/2,this._height/2,this._lastButton,tmpMat,currProjMat.mult(tmpMat));}
else{this._scene._nameSpace.doc.ctx.pickValue(this,this._width/2,this._height/2,this._lastButton);}
if(this._pickingInfo.pickObj)
{dist=this._pickingInfo.pickPos.subtract(this._from).length();if(dist<=avatarRadius){step=0;}}
lv=this._at.subtract(this._from).normalize().multiply(step);this._at=this._at.add(lv);this._from=this._from.add(lv);if(navi._vf.type[0].toLowerCase()==="walk")
{tmpAt=this._from.addScaled(up,-1.0);tmpUp=sv.cross(up.negate()).normalize();tmpMat=x3dom.fields.SFMatrix4f.lookAt(this._from,tmpAt,tmpUp);tmpMat=tmpMat.inverse();this._scene._nameSpace.doc.ctx.pickValue(this,this._width/2,this._height/2,this._lastButton,tmpMat,currProjMat.mult(tmpMat));if(this._pickingInfo.pickObj)
{dist=this._pickingInfo.pickPos.subtract(this._from).length();this._at=this._at.add(up.multiply(avatarHeight-dist));this._from=this._from.add(up.multiply(avatarHeight-dist));}}
this._pickingInfo.pickObj=null;}
this._flyMat=x3dom.fields.SFMatrix4f.lookAt(this._from,this._at,up);this._scene.getViewpoint().setView(this._flyMat.inverse());}
return needNavAnim;};x3dom.Viewarea.prototype.moveFwd=function()
{var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].toLowerCase()==="game")
{var avatarRadius=0.25;var avatarHeight=1.6;if(navi._vf.avatarSize.length>2){avatarRadius=navi._vf.avatarSize[0];avatarHeight=navi._vf.avatarSize[1];}
var speed=5*this._deltaT*navi._vf.speed;var yRotRad=(this._yaw/180*Math.PI);var xRotRad=(this._pitch/180*Math.PI);var dist=0;var fMat=this._flyMat.inverse();this._scene._nameSpace.doc.ctx.pickValue(this,this._width/2,this._height/2,this._lastButton);if(this._pickingInfo.pickObj)
{dist=this._pickingInfo.pickPos.subtract(fMat.e3()).length();if(dist<=2*avatarRadius){}
else{this._eyePos.x-=Math.sin(yRotRad)*speed;this._eyePos.z+=Math.cos(yRotRad)*speed;this._eyePos.y+=Math.sin(xRotRad)*speed;}}}};x3dom.Viewarea.prototype.moveBwd=function()
{var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].toLowerCase()==="game")
{var speed=5*this._deltaT*navi._vf.speed;var yRotRad=(this._yaw/180*Math.PI);var xRotRad=(this._pitch/180*Math.PI);this._eyePos.x+=Math.sin(yRotRad)*speed;this._eyePos.z-=Math.cos(yRotRad)*speed;this._eyePos.y-=Math.sin(xRotRad)*speed;}};x3dom.Viewarea.prototype.strafeRight=function()
{var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].toLowerCase()==="game")
{var speed=5*this._deltaT*navi._vf.speed;var yRotRad=(this._yaw/180*Math.PI);this._eyePos.x-=Math.cos(yRotRad)*speed;this._eyePos.z-=Math.sin(yRotRad)*speed;}};x3dom.Viewarea.prototype.strafeLeft=function()
{var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].toLowerCase()==="game")
{var speed=5*this._deltaT*navi._vf.speed;var yRotRad=(this._yaw/180*Math.PI);this._eyePos.x+=Math.cos(yRotRad)*speed;this._eyePos.z+=Math.sin(yRotRad)*speed;}};x3dom.Viewarea.prototype.animateTo=function(target,prev,dur)
{var navi=this._scene.getNavigationInfo();if(x3dom.isa(target,x3dom.nodeTypes.X3DViewpointNode)){target=target.getViewMatrix();}
if(navi._vf.transitionType[0].toLowerCase()!=="teleport"&&navi.getType()!=="game")
{if(prev&&x3dom.isa(prev,x3dom.nodeTypes.X3DViewpointNode)){prev=prev.getCurrentTransform().mult(prev.getViewMatrix()).mult(this._transMat).mult(this._rotMat);this._mixer._beginTime=this._lastTS;if(arguments.length>=3){this._mixer._endTime=this._lastTS+dur;}
else{this._mixer._endTime=this._lastTS+navi._vf.transitionTime;}
this._mixer.setBeginMatrix(prev);this._mixer.setEndMatrix(target);this._scene.getViewpoint().setView(prev);}
else{this._scene.getViewpoint().setView(target);}}
else
{this._scene.getViewpoint().setView(target);}
this._rotMat=x3dom.fields.SFMatrix4f.identity();this._transMat=x3dom.fields.SFMatrix4f.identity();this._movement=new x3dom.fields.SFVec3f(0,0,0);this._relMat=x3dom.fields.SFMatrix4f.identity();this._needNavigationMatrixUpdate=true;};x3dom.Viewarea.prototype.getLights=function(){return this._doc._nodeBag.lights;};x3dom.Viewarea.prototype.getLightsShadow=function(){var lights=this._doc._nodeBag.lights;for(var l=0;l<lights.length;l++){if(lights[l]._vf.shadowIntensity>0.0){return true;}}
return false;};x3dom.Viewarea.prototype.updateSpecialNavigation=function(viewpoint,mat_viewpoint){var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].toLowerCase()=="helicopter"&&!navi._heliUpdated)
{var typeParams=navi.getTypeParams();var theta=typeParams[0];var currViewMat=viewpoint.getViewMatrix().mult(mat_viewpoint.inverse()).inverse();this._from=currViewMat.e3();this._at=this._from.subtract(currViewMat.e2());this._up=new x3dom.fields.SFVec3f(0,1,0);this._from.y=typeParams[1];this._at.y=this._from.y;var sv=currViewMat.e0();var q=x3dom.fields.Quaternion.axisAngle(sv,theta);var temp=q.toMatrix();var fin=x3dom.fields.SFMatrix4f.translation(this._from);fin=fin.mult(temp);temp=x3dom.fields.SFMatrix4f.translation(this._from.negate());fin=fin.mult(temp);this._at=fin.multMatrixPnt(this._at);this._flyMat=x3dom.fields.SFMatrix4f.lookAt(this._from,this._at,this._up);this._scene.getViewpoint().setView(this._flyMat.inverse());navi._heliUpdated=true;}};x3dom.Viewarea.prototype.getViewpointMatrix=function(){var viewpoint=this._scene.getViewpoint();var mat_viewpoint=viewpoint.getCurrentTransform();this.updateSpecialNavigation(viewpoint,mat_viewpoint);return viewpoint.getViewMatrix().mult(mat_viewpoint.inverse());};x3dom.Viewarea.prototype.getViewMatrix=function(){return this.getViewpointMatrix().mult(this._transMat).mult(this._rotMat);};x3dom.Viewarea.prototype.getLightMatrix=function()
{var lights=this._doc._nodeBag.lights;var i,n=lights.length;if(n>0)
{var min=x3dom.fields.SFVec3f.MAX();var max=x3dom.fields.SFVec3f.MIN();var ok=this._scene.getVolume(min,max);if(ok)
{var l_arr=[];var viewpoint=this._scene.getViewpoint();var fov=viewpoint.getFieldOfView();var dia=max.subtract(min);var dist1=(dia.y/2.0)/Math.tan(fov/2.0)+(dia.z/2.0);var dist2=(dia.x/2.0)/Math.tan(fov/2.0)+(dia.z/2.0);dia=min.add(dia.multiply(0.5));for(i=0;i<n;i++)
{if(x3dom.isa(lights[i],x3dom.nodeTypes.PointLight)){var wcLoc=lights[i].getCurrentTransform().multMatrixPnt(lights[i]._vf.location);dia=dia.subtract(wcLoc).normalize();}
else{var dir=lights[i].getCurrentTransform().multMatrixVec(lights[i]._vf.direction);dir=dir.normalize().negate();dia=dia.add(dir.multiply(1.2*(dist1>dist2?dist1:dist2)));}
l_arr[i]=lights[i].getViewMatrix(dia);}
return l_arr;}}
return[this.getViewMatrix()];};x3dom.Viewarea.prototype.getWCtoLCMatrix=function(lMat)
{var proj=this.getProjectionMatrix();var view;if(arguments.length===0){view=this.getLightMatrix()[0];}
else{view=lMat;}
return proj.mult(view);};x3dom.Viewarea.prototype.getProjectionMatrix=function()
{var viewpoint=this._scene.getViewpoint();return viewpoint.getProjectionMatrix(this._width/this._height);};x3dom.Viewarea.prototype.getViewfrustum=function(clipMat)
{if(this._scene._vf.frustumCulling==true)
{if(arguments.length==0){var proj=this.getProjectionMatrix();var view=this.getViewMatrix();return new x3dom.fields.FrustumVolume(proj.mult(view));}
else{return new x3dom.fields.FrustumVolume(clipMat);}}
else{return null;}};x3dom.Viewarea.prototype.getWCtoCCMatrix=function()
{var view=this.getViewMatrix();var proj=this.getProjectionMatrix();return proj.mult(view);};x3dom.Viewarea.prototype.getCCtoWCMatrix=function()
{var mat=this.getWCtoCCMatrix();return mat.inverse();};x3dom.Viewarea.prototype.calcViewRay=function(x,y)
{var cctowc=this.getCCtoWCMatrix();var rx=x/(this._width-1.0)*2.0-1.0;var ry=(this._height-1.0-y)/(this._height-1.0)*2.0-1.0;var from=cctowc.multFullMatrixPnt(new x3dom.fields.SFVec3f(rx,ry,-1));var at=cctowc.multFullMatrixPnt(new x3dom.fields.SFVec3f(rx,ry,1));var dir=at.subtract(from);return new x3dom.fields.Line(from,dir);};x3dom.Viewarea.prototype.showAll=
function(axis) {
    var min = x3dom.fields.SFVec3f.MAX();
    var max = x3dom.fields.SFVec3f.MIN();

    var ok = this._scene.getVolume(min, max, true);

    if (ok)
    {
        // assume FOV_smaller as camera's fovMode
        var focalLen = (this._width < this._height) ?
            this._width : this._height;

        var n0 = new x3dom.fields.SFVec3f(0, 0, 1);    // facingDir
        var viewpoint = this._scene.getViewpoint();
        var fov = viewpoint.getFieldOfView() / 2.0;
        var ta = Math.tan(fov);

        if (Math.abs(ta) > x3dom.fields.Eps) {
            focalLen /= ta;
        }

        var w = this._width - 1;
        var h = this._height - 1;

        var frame = 0.25;
        var minScreenPos = new x3dom.fields.SFVec2f(frame * w, frame * h);

        frame = 0.75;
        var maxScreenPos = new x3dom.fields.SFVec2f(frame * w, frame * h);

        var dia2 = max.subtract(min).multiply(0.5);     // half diameter
        var rw = dia2.length();                         // approx radius

        var pc = min.add(dia2);                         // center in wc
        var vc = maxScreenPos.subtract(minScreenPos).multiply(0.5);

        var rs = 1.5 * vc.length();
        vc = vc.add(minScreenPos);

        var dist = 1.0;
        if (rs > x3dom.fields.Eps) {
            dist = (rw / rs) * Math.sqrt(vc.x*vc.x + vc.y*vc.y + focalLen*focalLen);
        }

        n0 = n0.normalize();
        n0 = n0.multiply(dist);
        var p0 = pc.add(n0);

        var qDir = x3dom.fields.Quaternion.rotateFromTo(new x3dom.fields.SFVec3f(0, 0, 1), n0);
        var R = qDir.toMatrix();

        var T = x3dom.fields.SFMatrix4f.translation(p0.negate());
        var M = x3dom.fields.SFMatrix4f.translation(p0);

        M = M.mult(R).mult(T).mult(M);
        var viewmat = M.inverse();

        this.animateTo(viewmat, viewpoint);
    }
};x3dom.Viewarea.prototype.resetView=function()
{var navi=this._scene.getNavigationInfo();if(navi._vf.transitionType[0].toLowerCase()!=="teleport"&&navi.getType()!=="game")
{this._mixer._beginTime=this._lastTS;this._mixer._endTime=this._lastTS+navi._vf.transitionTime;this._mixer.setBeginMatrix(this.getViewMatrix());this._scene.getViewpoint().resetView();this._mixer.setEndMatrix(this._scene.getViewpoint().getViewMatrix());}
else
{this._scene.getViewpoint().resetView();}
this._rotMat=x3dom.fields.SFMatrix4f.identity();this._transMat=x3dom.fields.SFMatrix4f.identity();this._movement=new x3dom.fields.SFVec3f(0,0,0);this._relMat=x3dom.fields.SFMatrix4f.identity();this._needNavigationMatrixUpdate=true;navi._heliUpdated=false;};x3dom.Viewarea.prototype.uprightView=function()
{var mat=this.getViewMatrix().inverse();var from=mat.e3();var at=from.subtract(mat.e2());var up=new x3dom.fields.SFVec3f(0,1,0);var s=mat.e2().cross(up).normalize();var v=s.cross(up).normalize();at=from.add(v);mat=x3dom.fields.SFMatrix4f.lookAt(from,at,up);mat=mat.inverse();this.animateTo(mat,this._scene.getViewpoint());};x3dom.Viewarea.prototype.callEvtHandler=function(node,eventType,event)
{if(!node||!node._xmlNode)
return;event.target=node._xmlNode;var attrib=node._xmlNode[eventType];try{if(typeof(attrib)==="function"){attrib.call(node._xmlNode,event);}
else{var funcStr=node._xmlNode.getAttribute(eventType);var func=new Function('event',funcStr);func.call(node._xmlNode,event);}
var list=node._listeners[event.type];if(list){for(var it=0;it<list.length;it++){list[it].call(node._xmlNode,event);}}}
catch(e){x3dom.debug.logException(e);}
return event.cancelBubble;};x3dom.Viewarea.prototype.checkEvents=function(obj,x,y,buttonState,eventType)
{var that=this;var needRecurse=true;var event={target:{},type:eventType.substr(2,eventType.length-2),button:buttonState,layerX:x,layerY:y,worldX:that._pick.x,worldY:that._pick.y,worldZ:that._pick.z,normalX:that._pickNorm.x,normalY:that._pickNorm.y,normalZ:that._pickNorm.z,hitPnt:that._pick.toGL(),hitObject:obj._xmlNode?obj._xmlNode:null,cancelBubble:false,stopPropagation:function(){this.cancelBubble=true;},preventDefault:function(){this.cancelBubble=true;}};try{var anObj=obj;if(anObj&&anObj._xmlNode&&anObj._cf.geometry&&!anObj._xmlNode[eventType]&&!anObj._xmlNode.hasAttribute(eventType)&&!anObj._listeners[event.type]){anObj=anObj._cf.geometry.node;}
if(anObj&&that.callEvtHandler(anObj,eventType,event)===true){needRecurse=false;}}
catch(e){x3dom.debug.logException(e);}
var recurse=function(obj){Array.forEach(obj._parentNodes,function(node){if(node._xmlNode&&(node._xmlNode[eventType]||node._xmlNode.hasAttribute(eventType)||node._listeners[event.type]))
{if(that.callEvtHandler(node,eventType,event)===true){needRecurse=false;}}
if(x3dom.isa(node,x3dom.nodeTypes.Anchor)&&eventType==='onclick'){node.handleTouch();needRecurse=false;}
else if(needRecurse){recurse(node);}});};if(needRecurse){recurse(obj);}
return needRecurse;};x3dom.Viewarea.prototype.initMouseState=function()
{this._deltaT=0;this._dx=0;this._dy=0;this._lastX=-1;this._lastY=-1;this._pressX=-1;this._pressY=-1;this._lastButton=0;this._needNavigationMatrixUpdate=true;};x3dom.Viewarea.prototype.onMousePress=function(x,y,buttonState)
{this._needNavigationMatrixUpdate=true;this.prepareEvents(x,y,buttonState,"onmousedown");this._pickingInfo.lastClickObj=this._pickingInfo.pickObj;this._dx=0;this._dy=0;this._lastX=x;this._lastY=y;this._pressX=x;this._pressY=y;this._lastButton=buttonState;};x3dom.Viewarea.prototype.onMouseRelease=function(x,y,buttonState)
{var tDist=3.0;var dir;var navi=this._scene.getNavigationInfo();if(this._scene._vf.pickMode.toLowerCase()!=="box"){this.prepareEvents(x,y,buttonState,"onmouseup");if(this._pickingInfo.pickObj&&this._pickingInfo.pickObj===this._pickingInfo.lastClickObj){this.prepareEvents(x,y,buttonState,"onclick");}}
else{var t0=new Date().getTime();var line=this.calcViewRay(x,y);var isect=this._scene.doIntersect(line);var obj=line.hitObject;if(isect&&obj)
{this._pick.setValues(line.hitPoint);this.checkEvents(obj,x,y,buttonState,"onclick");x3dom.debug.logInfo("Hit '"+obj._xmlNode.localName+"/ "+
obj._DEF+"' at dist="+line.dist.toFixed(4));x3dom.debug.logInfo("Ray hit at position "+this._pick);}
var t1=new Date().getTime()-t0;x3dom.debug.logInfo("Picking time (box): "+t1+"ms");if(!isect){dir=this.getViewMatrix().e2().negate();var u=dir.dot(line.pos.negate())/dir.dot(line.dir);this._pick=line.pos.add(line.dir.multiply(u));}}
if(this._pickingInfo.pickObj&&navi._vf.type[0].toLowerCase()==="lookat"&&this._pressX===x&&this._pressY===y)
{var step=(this._lastButton&2)?-1:1;var dist=this._pickingInfo.pickPos.subtract(this._from).length()/tDist;var laMat=new x3dom.fields.SFMatrix4f();laMat.setValues(this.getViewMatrix());laMat=laMat.inverse();var from=laMat.e3();var at=from.subtract(laMat.e2());var up=laMat.e1();dir=this._pickingInfo.pickPos.subtract(from);var len=dir.length();dir=dir.normalize();var newAt=from.addScaled(dir,len);var s=dir.cross(up).normalize();dir=s.cross(up).normalize();if(step<0){dist=(0.5+len+dist)*2;}
var newFrom=newAt.addScaled(dir,dist);laMat=x3dom.fields.SFMatrix4f.lookAt(newFrom,newAt,up);laMat=laMat.inverse();dist=newFrom.subtract(from).length();var dur=Math.max(0.5,Math.log((1+dist)/navi._vf.speed));this.animateTo(laMat,this._scene.getViewpoint(),dur);}
this._dx=0;this._dy=0;this._lastX=x;this._lastY=y;this._lastButton=buttonState;};x3dom.Viewarea.prototype.onMouseOver=function(x,y,buttonState)
{this._dx=0;this._dy=0;this._lastButton=0;this._lastX=x;this._lastY=y;this._deltaT=0;};x3dom.Viewarea.prototype.onMouseOut=function(x,y,buttonState)
{this._dx=0;this._dy=0;this._lastButton=0;this._lastX=x;this._lastY=y;this._deltaT=0;};x3dom.Viewarea.prototype.onDoubleClick=function(x,y)
{if(this._doc.properties.getProperty('disableDoubleClick','false')==='true'){return;}
var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].length<=1||navi._vf.type[0].toLowerCase()=="none"){return;}
if((this._scene._vf.pickMode.toLowerCase()==="color"||this._scene._vf.pickMode.toLowerCase()==="texcoord")){return;}
var viewpoint=this._scene.getViewpoint();viewpoint._vf.centerOfRotation.setValues(this._pick);x3dom.debug.logInfo("New center of Rotation:  "+this._pick);var mat=this.getViewMatrix().inverse();var from=mat.e3();var at=this._pick;var up=mat.e1();var norm=mat.e0().cross(up).normalize();var dist=norm.dot(this._pick.subtract(from));from=at.addScaled(norm,-dist);mat=x3dom.fields.SFMatrix4f.lookAt(from,at,up);x3dom.debug.logInfo("New camera position:  "+from);this.animateTo(mat.inverse(),viewpoint);};x3dom.Viewarea.prototype.handleMoveEvt=function(x,y,buttonState)
{this.prepareEvents(x,y,buttonState,"onmousemove");if(this._pickingInfo.pickObj!==this._pickingInfo.lastObj)
{if(this._pickingInfo.lastObj){var obj=this._pickingInfo.pickObj;this._pickingInfo.pickObj=this._pickingInfo.lastObj;this.prepareEvents(x,y,buttonState,"onmouseout");this._pickingInfo.pickObj=obj;}
if(this._pickingInfo.pickObj){this.prepareEvents(x,y,buttonState,"onmouseover");}
this._pickingInfo.lastObj=this._pickingInfo.pickObj;}};x3dom.Viewarea.prototype.onMove=function(x,y,buttonState)
{this.handleMoveEvt(x,y,buttonState);if(this._lastX<0||this._lastY<0){this._lastX=x;this._lastY=y;}
this._dx=x-this._lastX;this._dy=y-this._lastY;this._lastX=x;this._lastY=y;};x3dom.Viewarea.prototype.onMoveView=function(translation,rotation)
{var navi=this._scene.getNavigationInfo();var viewpoint=this._scene.getViewpoint();if(navi._vf.type[0].toLowerCase()==="examine")
{if(translation)
{var distance=10;if(this._scene._lastMin&&this._scene._lastMax)
{distance=(this._scene._lastMax.subtract(this._scene._lastMin)).length();distance=(distance<x3dom.fields.Eps)?1:distance;}
translation=translation.multiply(distance);this._movement=this._movement.add(translation);this._transMat=viewpoint.getViewMatrix().inverse().mult(x3dom.fields.SFMatrix4f.translation(this._movement)).mult(viewpoint.getViewMatrix());}
if(rotation)
{var center=viewpoint.getCenterOfRotation();var mat=this.getViewMatrix();mat.setTranslate(new x3dom.fields.SFVec3f(0,0,0));this._rotMat=this._rotMat.mult(x3dom.fields.SFMatrix4f.translation(center)).mult(mat.inverse()).mult(rotation).mult(mat).mult(x3dom.fields.SFMatrix4f.translation(center.negate()));}}};x3dom.Viewarea.prototype.onDrag=function(x,y,buttonState)
{this.handleMoveEvt(x,y,buttonState);var navi=this._scene.getNavigationInfo();if(navi._vf.type[0].length<=1||navi._vf.type[0].toLowerCase()==="none"){return;}
var dx=x-this._lastX;var dy=y-this._lastY;var min,max,ok,d,vec;var mat=null;if(navi._vf.type[0].toLowerCase()==="examine")
{if(buttonState&1)
{var alpha=(dy*2*Math.PI)/this._width;var beta=(dx*2*Math.PI)/this._height;mat=this.getViewMatrix();var mx=x3dom.fields.SFMatrix4f.rotationX(alpha);var my=x3dom.fields.SFMatrix4f.rotationY(beta);var viewpoint=this._scene.getViewpoint();var center=viewpoint.getCenterOfRotation();mat.setTranslate(new x3dom.fields.SFVec3f(0,0,0));this._rotMat=this._rotMat.mult(x3dom.fields.SFMatrix4f.translation(center)).mult(mat.inverse()).mult(mx).mult(my).mult(mat).mult(x3dom.fields.SFMatrix4f.translation(center.negate()));}
if(buttonState&4)
{if(this._scene._lastMin&&this._scene._lastMax)
{d=(this._scene._lastMax.subtract(this._scene._lastMin)).length();}
else
{min=x3dom.fields.SFVec3f.MAX();max=x3dom.fields.SFVec3f.MIN();ok=this._scene.getVolume(min,max);if(ok){this._scene._lastMin=min;this._scene._lastMax=max;}
d=ok?(max.subtract(min)).length():10;}
d=((d<x3dom.fields.Eps)?1:d)*navi._vf.speed;vec=new x3dom.fields.SFVec3f(d*dx/this._width,d*(-dy)/this._height,0);this._movement=this._movement.add(vec);mat=this.getViewpointMatrix().mult(this._transMat);this._transMat=mat.inverse().mult(x3dom.fields.SFMatrix4f.translation(this._movement)).mult(mat);}
if(buttonState&2)
{if(this._scene._lastMin&&this._scene._lastMax)
{d=(this._scene._lastMax.subtract(this._scene._lastMin)).length();}
else
{min=x3dom.fields.SFVec3f.MAX();max=x3dom.fields.SFVec3f.MIN();ok=this._scene.getVolume(min,max);if(ok){this._scene._lastMin=min;this._scene._lastMax=max;}
d=ok?(max.subtract(min)).length():10;}
d=((d<x3dom.fields.Eps)?1:d)*navi._vf.speed;vec=new x3dom.fields.SFVec3f(0,0,d*(dx+dy)/this._height);this._movement=this._movement.add(vec);mat=this.getViewpointMatrix().mult(this._transMat);this._transMat=mat.inverse().mult(x3dom.fields.SFMatrix4f.translation(this._movement)).mult(mat);}}
this._dx=dx;this._dy=dy;this._lastX=x;this._lastY=y;};x3dom.Viewarea.prototype.prepareEvents=function(x,y,buttonState,eventType)
{var avoidTraversal=(this._scene._vf.pickMode.toLowerCase().indexOf("idbuf")==0||this._scene._vf.pickMode.toLowerCase()==="color"||this._scene._vf.pickMode.toLowerCase()==="texcoord");if(avoidTraversal){var obj=this._pickingInfo.pickObj;if(obj){this._pick.setValues(this._pickingInfo.pickPos);this._pickNorm.setValues(this._pickingInfo.pickNorm);this.checkEvents(obj,x,y,buttonState,eventType);if(eventType==="onclick"){if(obj._xmlNode)
x3dom.debug.logInfo("Hit \""+obj._xmlNode.localName+"/ "+obj._DEF+"\"");x3dom.debug.logInfo("Ray hit at position "+this._pick);}}}};x3dom.Viewarea.prototype.rotateH=function(right)
{this._relMat=this._relMat.mult(x3dom.fields.SFMatrix4f.parseRotation("0, 1, 0,"+(right?"-0.05":"0.05")));};x3dom.Viewarea.prototype.rotateV=function(down)
{this._relMat=this._relMat.mult(x3dom.fields.SFMatrix4f.parseRotation("1, 0, 0,"+(down?"-0.05":"0.05")));};x3dom.Viewarea.prototype.pan=function(vec)
{this._relMat=this._relMat.mult(x3dom.fields.SFMatrix4f.translation(vec));};x3dom.Viewarea.prototype.zoom=function(vec)
{this._relMat=this._relMat.mult(x3dom.fields.SFMatrix4f.translation(vec));};x3dom.Viewarea.prototype.orbitH=function(dx)
{var beta=-(dx*2*Math.PI)/this._height;var mat=this.getViewMatrix().inverse();var pos=mat.e3();var at=pos.subtract(mat.e2());var up=mat.e1();var viewpoint=this._scene.getViewpoint();var center=viewpoint.getCenterOfRotation();var radius=center.subtract(pos);var m=new x3dom.fields.SFMatrix4f.translation(radius);m=m.mult(x3dom.fields.SFMatrix4f.parseRotation(up.x+", "+up.y+", "+up.z+", "+beta));m=m.mult(x3dom.fields.SFMatrix4f.translation(radius.negate()));pos=center.subtract(m.multMatrixVec(radius));at=m.multMatrixVec(at);up=m.multMatrixVec(up);m=x3dom.fields.SFMatrix4f.lookAt(pos,at,up);this._relMat=this.getViewpointMatrix().mult(m);};x3dom.Viewarea.prototype.orbitV=function(dy)
{var alpha=(dy*2*Math.PI)/this._width;var mat=this.getViewMatrix().inverse();var pos=mat.e3();var at=pos.subtract(mat.e2());var up=mat.e1();var viewpoint=this._scene.getViewpoint();var center=viewpoint.getCenterOfRotation();var radius=center.subtract(pos);var m=new x3dom.fields.SFMatrix4f.translation(radius);var rotVec=at.cross(up);m=m.mult(x3dom.fields.SFMatrix4f.parseRotation(rotVec.x+", "+rotVec.y+", "+rotVec.z+", "+alpha));m=m.mult(x3dom.fields.SFMatrix4f.translation(radius.negate()));pos=center.subtract(m.multMatrixVec(radius));at=m.multMatrixVec(at);up=m.multMatrixVec(up);m=x3dom.fields.SFMatrix4f.lookAt(pos,at,up);this._relMat=this.getViewpointMatrix().mult(m);};x3dom.Mesh=function(parent)
{this._parent=parent;this._vol=new x3dom.fields.BoxVolume();this._invalidate=true;this._numFaces=0;this._numCoords=0;this._primType='TRIANGLES';this._positions=[];this._normals=[];this._texCoords=[];this._colors=[];this._indices=[];this._positions[0]=[];this._normals[0]=[];this._texCoords[0]=[];this._colors[0]=[];this._indices[0]=[];};x3dom.Mesh.prototype._dynamicFields={};x3dom.Mesh.prototype._numPosComponents=3;x3dom.Mesh.prototype._numTexComponents=2;x3dom.Mesh.prototype._numColComponents=3;x3dom.Mesh.prototype._numNormComponents=3;x3dom.Mesh.prototype._lit=true;x3dom.Mesh.prototype._vol=null;x3dom.Mesh.prototype._invalidate=true;x3dom.Mesh.prototype._numFaces=0;x3dom.Mesh.prototype._numCoords=0;x3dom.Mesh.prototype.setMeshData=function(positions,normals,texCoords,colors,indices)
{this._positions[0]=positions;this._normals[0]=normals;this._texCoords[0]=texCoords;this._colors[0]=colors;this._indices[0]=indices;this._invalidate=true;this._numFaces=this._indices[0].length/3;this._numCoords=this._positions[0].length/3;};x3dom.Mesh.prototype.getVolume=function(min,max)
{if(this._invalidate==true&&!this._vol.isValid())
{var coords=this._positions[0];var n=coords.length;if(n>3)
{var initVal=new x3dom.fields.SFVec3f(coords[0],coords[1],coords[2]);this._vol.setBounds(initVal,initVal);for(var i=3;i<n;i+=3)
{if(this._vol.min.x>coords[i]){this._vol.min.x=coords[i];}
if(this._vol.min.y>coords[i+1]){this._vol.min.y=coords[i+1];}
if(this._vol.min.z>coords[i+2]){this._vol.min.z=coords[i+2];}
if(this._vol.max.x<coords[i]){this._vol.max.x=coords[i];}
if(this._vol.max.y<coords[i+1]){this._vol.max.y=coords[i+1];}
if(this._vol.max.z<coords[i+2]){this._vol.max.z=coords[i+2];}}
this._invalidate=false;}}
this._vol.getBounds(min,max);return this._vol.isValid();};x3dom.Mesh.prototype.invalidate=function()
{this._invalidate=true;this._vol.invalidate();};x3dom.Mesh.prototype.isValid=function()
{return this._vol.isValid();};x3dom.Mesh.prototype.getCenter=function()
{var min=new x3dom.fields.SFVec3f(0,0,0);var max=new x3dom.fields.SFVec3f(0,0,0);this.getVolume(min,max);return(min.add(max)).multiply(0.5);};x3dom.Mesh.prototype.getDiameter=function()
{var min=new x3dom.fields.SFVec3f(0,0,0);var max=new x3dom.fields.SFVec3f(0,0,0);this.getVolume(min,max);var size=max.subtract(min);return size.length();};x3dom.Mesh.prototype.doIntersect=function(line)
{var min=new x3dom.fields.SFVec3f(0,0,0);var max=new x3dom.fields.SFVec3f(0,0,0);this.getVolume(min,max);var isect=line.intersect(min,max);if(isect&&line.enter<line.dist)
{line.dist=line.enter;line.hitObject=this._parent;line.hitPoint=line.pos.add(line.dir.multiply(line.enter));}
return isect;};x3dom.Mesh.prototype.calcNormals=function(creaseAngle)
{var i=0,j=0,num=0;var multInd=(this._multiIndIndices!==undefined&&this._multiIndIndices.length);var coords=this._positions[0];var idxs=multInd?this._multiIndIndices:this._indices[0];var vertNormals=[];var vertFaceNormals=[];var a,b,n=null;num=(this._posSize!==undefined&&this._posSize>coords.length)?this._posSize/3:coords.length/3;num=3*((num-Math.floor(num)>0)?Math.floor(num+1):num);for(i=0;i<num;++i){vertFaceNormals[i]=[];}
num=idxs.length;for(i=0;i<num;i+=3){if(!multInd){a=new x3dom.fields.SFVec3f(coords[idxs[i]*3],coords[idxs[i]*3+1],coords[idxs[i]*3+2]).subtract(new x3dom.fields.SFVec3f(coords[idxs[i+1]*3],coords[idxs[i+1]*3+1],coords[idxs[i+1]*3+2]));b=new x3dom.fields.SFVec3f(coords[idxs[i+1]*3],coords[idxs[i+1]*3+1],coords[idxs[i+1]*3+2]).subtract(new x3dom.fields.SFVec3f(coords[idxs[i+2]*3],coords[idxs[i+2]*3+1],coords[idxs[i+2]*3+2]));}
else{a=new x3dom.fields.SFVec3f(coords[i*3],coords[i*3+1],coords[i*3+2]).subtract(new x3dom.fields.SFVec3f(coords[(i+1)*3],coords[(i+1)*3+1],coords[(i+1)*3+2]));b=new x3dom.fields.SFVec3f(coords[(i+1)*3],coords[(i+1)*3+1],coords[(i+1)*3+2]).subtract(new x3dom.fields.SFVec3f(coords[(i+2)*3],coords[(i+2)*3+1],coords[(i+2)*3+2]));}
n=a.cross(b).normalize();if(creaseAngle<=x3dom.fields.Eps){vertNormals[i*3]=vertNormals[(i+1)*3]=vertNormals[(i+2)*3]=n.x;vertNormals[i*3+1]=vertNormals[(i+1)*3+1]=vertNormals[(i+2)*3+1]=n.y;vertNormals[i*3+2]=vertNormals[(i+1)*3+2]=vertNormals[(i+2)*3+2]=n.z;}
else{vertFaceNormals[idxs[i]].push(n);vertFaceNormals[idxs[i+1]].push(n);vertFaceNormals[idxs[i+2]].push(n);}}
if(creaseAngle>x3dom.fields.Eps)
{for(i=0;i<coords.length;i+=3){n=new x3dom.fields.SFVec3f(0,0,0);if(!multInd){num=vertFaceNormals[i/3].length;for(j=0;j<num;++j){n=n.add(vertFaceNormals[i/3][j]);}}
else{num=vertFaceNormals[idxs[i/3]].length;for(j=0;j<num;++j){n=n.add(vertFaceNormals[idxs[i/3]][j]);}}
n=n.normalize();vertNormals[i]=n.x;vertNormals[i+1]=n.y;vertNormals[i+2]=n.z;}}
if(multInd){this._multiIndIndices=[];}
this._normals[0]=vertNormals;};x3dom.Mesh.prototype.splitMesh=function()
{var MAX=65535;if(this._positions[0].length/3<=MAX){return;}
var positions=this._positions[0];var normals=this._normals[0];var texCoords=this._texCoords[0];var colors=this._colors[0];var indices=this._indices[0];var i=0;do
{this._positions[i]=[];this._normals[i]=[];this._texCoords[i]=[];this._colors[i]=[];this._indices[i]=[];var k=(indices.length-((i+1)*MAX)>=0);if(k){this._indices[i]=indices.slice(i*MAX,(i+1)*MAX);}else{this._indices[i]=indices.slice(i*MAX);}
if(i){var m=i*MAX;for(var j=0,l=this._indices[i].length;j<l;j++){this._indices[i][j]-=m;}}
if(k){this._positions[i]=positions.slice(i*MAX*3,3*(i+1)*MAX);}else{this._positions[i]=positions.slice(i*MAX*3);}
if(normals.length){if(k){this._normals[i]=normals.slice(i*MAX*3,3*(i+1)*MAX);}else{this._normals[i]=normals.slice(i*MAX*3);}}
if(texCoords.length){if(k){this._texCoords[i]=texCoords.slice(i*MAX*this._numTexComponents,this._numTexComponents*(i+1)*MAX);}else{this._texCoords[i]=texCoords.slice(i*MAX*this._numTexComponents);}}
if(colors.length){if(k){this._colors[i]=colors.slice(i*MAX*this._numColComponents,this._numColComponents*(i+1)*MAX);}else{this._colors[i]=colors.slice(i*MAX*this._numColComponents);}}}
while(positions.length>++i*MAX*3);};x3dom.Mesh.prototype.calcTexCoords=function(mode)
{this._texCoords[0]=[];if(mode.toLowerCase()==="sphere-local")
{for(var i=0,j=0,n=this._normals[0].length;i<n;i+=3)
{this._texCoords[0][j++]=0.5+this._normals[0][i]/2.0;this._texCoords[0][j++]=0.5+this._normals[0][i+1]/2.0;}}
else
{var min=new x3dom.fields.SFVec3f(0,0,0),max=new x3dom.fields.SFVec3f(0,0,0);this.getVolume(min,max);var dia=max.subtract(min);var S=0,T=1;if(dia.x>=dia.y)
{if(dia.x>=dia.z)
{S=0;T=dia.y>=dia.z?1:2;}
else
{S=2;T=0;}}
else
{if(dia.y>=dia.z)
{S=1;T=dia.x>=dia.z?0:2;}
else
{S=2;T=1;}}
var sDenom=1,tDenom=1;var sMin=0,tMin=0;switch(S){case 0:sDenom=dia.x;sMin=min.x;break;case 1:sDenom=dia.y;sMin=min.y;break;case 2:sDenom=dia.z;sMin=min.z;break;}
switch(T){case 0:tDenom=dia.x;tMin=min.x;break;case 1:tDenom=dia.y;tMin=min.y;break;case 2:tDenom=dia.z;tMin=min.z;break;}
for(var k=0,l=0,m=this._positions[0].length;k<m;k+=3)
{this._texCoords[0][l++]=(this._positions[0][k+S]-sMin)/sDenom;this._texCoords[0][l++]=(this._positions[0][k+T]-tMin)/tDenom;}}};x3dom.fields={};x3dom.fields.Eps=0.000001;x3dom.fields.SFMatrix4f=function(_00,_01,_02,_03,_10,_11,_12,_13,_20,_21,_22,_23,_30,_31,_32,_33)
{if(arguments.length===0){this._00=1;this._01=0;this._02=0;this._03=0;this._10=0;this._11=1;this._12=0;this._13=0;this._20=0;this._21=0;this._22=1;this._23=0;this._30=0;this._31=0;this._32=0;this._33=1;}
else{this._00=_00;this._01=_01;this._02=_02;this._03=_03;this._10=_10;this._11=_11;this._12=_12;this._13=_13;this._20=_20;this._21=_21;this._22=_22;this._23=_23;this._30=_30;this._31=_31;this._32=_32;this._33=_33;}};x3dom.fields.SFMatrix4f.prototype.e0=function(){var baseVec=new x3dom.fields.SFVec3f(this._00,this._10,this._20);return baseVec.normalize();};x3dom.fields.SFMatrix4f.prototype.e1=function(){var baseVec=new x3dom.fields.SFVec3f(this._01,this._11,this._21);return baseVec.normalize();};x3dom.fields.SFMatrix4f.prototype.e2=function(){var baseVec=new x3dom.fields.SFVec3f(this._02,this._12,this._22);return baseVec.normalize();};x3dom.fields.SFMatrix4f.prototype.e3=function(){return new x3dom.fields.SFVec3f(this._03,this._13,this._23);};x3dom.fields.SFMatrix4f.copy=function(that){return new x3dom.fields.SFMatrix4f(that._00,that._01,that._02,that._03,that._10,that._11,that._12,that._13,that._20,that._21,that._22,that._23,that._30,that._31,that._32,that._33);};x3dom.fields.SFMatrix4f.identity=function(){return new x3dom.fields.SFMatrix4f(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);};x3dom.fields.SFMatrix4f.zeroMatrix=function(){return new x3dom.fields.SFMatrix4f(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);};x3dom.fields.SFMatrix4f.translation=function(vec){return new x3dom.fields.SFMatrix4f(1,0,0,vec.x,0,1,0,vec.y,0,0,1,vec.z,0,0,0,1);};x3dom.fields.SFMatrix4f.rotationX=function(a){var c=Math.cos(a);var s=Math.sin(a);return new x3dom.fields.SFMatrix4f(1,0,0,0,0,c,-s,0,0,s,c,0,0,0,0,1);};x3dom.fields.SFMatrix4f.rotationY=function(a){var c=Math.cos(a);var s=Math.sin(a);return new x3dom.fields.SFMatrix4f(c,0,s,0,0,1,0,0,-s,0,c,0,0,0,0,1);};x3dom.fields.SFMatrix4f.rotationZ=function(a){var c=Math.cos(a);var s=Math.sin(a);return new x3dom.fields.SFMatrix4f(c,-s,0,0,s,c,0,0,0,0,1,0,0,0,0,1);};x3dom.fields.SFMatrix4f.scale=function(vec){return new x3dom.fields.SFMatrix4f(vec.x,0,0,0,0,vec.y,0,0,0,0,vec.z,0,0,0,0,1);};x3dom.fields.SFMatrix4f.lookAt=function(from,at,up)
{var view=from.subtract(at).normalize();var right=up.normalize().cross(view);if(right.dot(right)<x3dom.fields.Eps){x3dom.debug.logWarning("View matrix is linearly dependent.");return x3dom.fields.SFMatrix4f.translation(from);}
var newUp=view.cross(right.normalize()).normalize();var tmp=x3dom.fields.SFMatrix4f.identity();tmp.setValue(right,newUp,view,from);return tmp;};x3dom.fields.SFMatrix4f.prototype.setTranslate=function(vec){this._03=vec.x;this._13=vec.y;this._23=vec.z;};x3dom.fields.SFMatrix4f.prototype.setScale=function(vec){this._00=vec.x;this._11=vec.y;this._22=vec.z;};x3dom.fields.SFMatrix4f.parseRotation=function(str){var m=/^([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)$/.exec(str);var x=+m[1],y=+m[2],z=+m[3],a=+m[4];var d=Math.sqrt(x*x+y*y+z*z);if(d===0){x=1;y=z=0;}else{x/=d;y/=d;z/=d;}
var c=Math.cos(a);var s=Math.sin(a);var t=1-c;return new x3dom.fields.SFMatrix4f(t*x*x+c,t*x*y+s*z,t*x*z-s*y,0,t*x*y-s*z,t*y*y+c,t*y*z+s*x,0,t*x*z+s*y,t*y*z-s*x,t*z*z+c,0,0,0,0,1).transpose();};x3dom.fields.SFMatrix4f.parse=function(str){var needTranspose=false;var val=/matrix.*\((.+)\)/;if(val.exec(str)){str=RegExp.$1;needTranspose=true;}
var arr=Array.map(str.split(/[,\s]+/),function(n){return+n;});if(arr.length>=16)
{if(!needTranspose){return new x3dom.fields.SFMatrix4f(arr[0],arr[1],arr[2],arr[3],arr[4],arr[5],arr[6],arr[7],arr[8],arr[9],arr[10],arr[11],arr[12],arr[13],arr[14],arr[15]);}
else{return new x3dom.fields.SFMatrix4f(arr[0],arr[4],arr[8],arr[12],arr[1],arr[5],arr[9],arr[13],arr[2],arr[6],arr[10],arr[14],arr[3],arr[7],arr[11],arr[15]);}}
else if(arr.length===6){return new x3dom.fields.SFMatrix4f(arr[0],arr[1],0,arr[4],arr[2],arr[3],0,arr[5],0,0,1,0,0,0,0,1);}
else{x3dom.debug.logWarning("SFMatrix4f - can't parse string: "+str);return x3dom.fields.SFMatrix4f.identity();}};x3dom.fields.SFMatrix4f.prototype.mult=function(that){return new x3dom.fields.SFMatrix4f(this._00*that._00+this._01*that._10+this._02*that._20+this._03*that._30,this._00*that._01+this._01*that._11+this._02*that._21+this._03*that._31,this._00*that._02+this._01*that._12+this._02*that._22+this._03*that._32,this._00*that._03+this._01*that._13+this._02*that._23+this._03*that._33,this._10*that._00+this._11*that._10+this._12*that._20+this._13*that._30,this._10*that._01+this._11*that._11+this._12*that._21+this._13*that._31,this._10*that._02+this._11*that._12+this._12*that._22+this._13*that._32,this._10*that._03+this._11*that._13+this._12*that._23+this._13*that._33,this._20*that._00+this._21*that._10+this._22*that._20+this._23*that._30,this._20*that._01+this._21*that._11+this._22*that._21+this._23*that._31,this._20*that._02+this._21*that._12+this._22*that._22+this._23*that._32,this._20*that._03+this._21*that._13+this._22*that._23+this._23*that._33,this._30*that._00+this._31*that._10+this._32*that._20+this._33*that._30,this._30*that._01+this._31*that._11+this._32*that._21+this._33*that._31,this._30*that._02+this._31*that._12+this._32*that._22+this._33*that._32,this._30*that._03+this._31*that._13+this._32*that._23+this._33*that._33);};x3dom.fields.SFMatrix4f.prototype.multMatrixPnt=function(vec){return new x3dom.fields.SFVec3f(this._00*vec.x+this._01*vec.y+this._02*vec.z+this._03,this._10*vec.x+this._11*vec.y+this._12*vec.z+this._13,this._20*vec.x+this._21*vec.y+this._22*vec.z+this._23);};x3dom.fields.SFMatrix4f.prototype.multMatrixVec=function(vec){return new x3dom.fields.SFVec3f(this._00*vec.x+this._01*vec.y+this._02*vec.z,this._10*vec.x+this._11*vec.y+this._12*vec.z,this._20*vec.x+this._21*vec.y+this._22*vec.z);};x3dom.fields.SFMatrix4f.prototype.multFullMatrixPnt=function(vec){var w=this._30*vec.x+this._31*vec.y+this._32*vec.z+this._33;if(w){w=1.0/w;}
return new x3dom.fields.SFVec3f((this._00*vec.x+this._01*vec.y+this._02*vec.z+this._03)*w,(this._10*vec.x+this._11*vec.y+this._12*vec.z+this._13)*w,(this._20*vec.x+this._21*vec.y+this._22*vec.z+this._23)*w);};x3dom.fields.SFMatrix4f.prototype.transpose=function(){return new x3dom.fields.SFMatrix4f(this._00,this._10,this._20,this._30,this._01,this._11,this._21,this._31,this._02,this._12,this._22,this._32,this._03,this._13,this._23,this._33);};x3dom.fields.SFMatrix4f.prototype.negate=function(){return new x3dom.fields.SFMatrix4f(-this._00,-this._01,-this._02,-this._03,-this._10,-this._11,-this._12,-this._13,-this._20,-this._21,-this._22,-this._23,-this._30,-this._31,-this._32,-this._33);};x3dom.fields.SFMatrix4f.prototype.multiply=function(s){return new x3dom.fields.SFMatrix4f(s*this._00,s*this._01,s*this._02,s*this._03,s*this._10,s*this._11,s*this._12,s*this._13,s*this._20,s*this._21,s*this._22,s*this._23,s*this._30,s*this._31,s*this._32,s*this._33);};x3dom.fields.SFMatrix4f.prototype.add=function(that){return new x3dom.fields.SFMatrix4f(this._00+that._00,this._01+that._01,this._02+that._02,this._03+that._03,this._10+that._10,this._11+that._11,this._12+that._12,this._13+that._13,this._20+that._20,this._21+that._21,this._22+that._22,this._23+that._23,this._30+that._30,this._31+that._31,this._32+that._32,this._33+that._33);};x3dom.fields.SFMatrix4f.prototype.addScaled=function(that,s){return new x3dom.fields.SFMatrix4f(this._00+s*that._00,this._01+s*that._01,this._02+s*that._02,this._03+s*that._03,this._10+s*that._10,this._11+s*that._11,this._12+s*that._12,this._13+s*that._13,this._20+s*that._20,this._21+s*that._21,this._22+s*that._22,this._23+s*that._23,this._30+s*that._30,this._31+s*that._31,this._32+s*that._32,this._33+s*that._33);};x3dom.fields.SFMatrix4f.prototype.setValues=function(that){this._00=that._00;this._01=that._01;this._02=that._02;this._03=that._03;this._10=that._10;this._11=that._11;this._12=that._12;this._13=that._13;this._20=that._20;this._21=that._21;this._22=that._22;this._23=that._23;this._30=that._30;this._31=that._31;this._32=that._32;this._33=that._33;};x3dom.fields.SFMatrix4f.prototype.setValue=function(v1,v2,v3,v4){this._00=v1.x;this._01=v2.x;this._02=v3.x;this._10=v1.y;this._11=v2.y;this._12=v3.y;this._20=v1.z;this._21=v2.z;this._22=v3.z;this._30=0;this._31=0;this._32=0;if(arguments.length>3){this._03=v4.x;this._13=v4.y;this._23=v4.z;this._33=1;}};x3dom.fields.SFMatrix4f.prototype.toGL=function(){return[this._00,this._10,this._20,this._30,this._01,this._11,this._21,this._31,this._02,this._12,this._22,this._32,this._03,this._13,this._23,this._33];};x3dom.fields.SFMatrix4f.prototype.at=function(i,j){var field="_"+i+j;return this[field];};x3dom.fields.SFMatrix4f.prototype.sqrt=function(){var Y=x3dom.fields.SFMatrix4f.identity();var result=x3dom.fields.SFMatrix4f.copy(this);for(var i=0;i<6;i++)
{var iX=result.inverse();var iY=(i==0)?x3dom.fields.SFMatrix4f.identity():Y.inverse();var rd=result.det(),yd=Y.det();var g=Math.abs(Math.pow(rd*yd,-0.125));var ig=1.0/g;result=result.multiply(g);result=result.addScaled(iY,ig);result=result.multiply(0.5);Y=Y.multiply(g);Y=Y.addScaled(iX,ig);Y=Y.multiply(0.5);}
return result;};x3dom.fields.SFMatrix4f.prototype.normInfinity=function(){var t=0,m=0;if((t=Math.abs(this._00))>m){m=t;}
if((t=Math.abs(this._01))>m){m=t;}
if((t=Math.abs(this._02))>m){m=t;}
if((t=Math.abs(this._03))>m){m=t;}
if((t=Math.abs(this._10))>m){m=t;}
if((t=Math.abs(this._11))>m){m=t;}
if((t=Math.abs(this._12))>m){m=t;}
if((t=Math.abs(this._13))>m){m=t;}
if((t=Math.abs(this._20))>m){m=t;}
if((t=Math.abs(this._21))>m){m=t;}
if((t=Math.abs(this._22))>m){m=t;}
if((t=Math.abs(this._23))>m){m=t;}
if((t=Math.abs(this._30))>m){m=t;}
if((t=Math.abs(this._31))>m){m=t;}
if((t=Math.abs(this._32))>m){m=t;}
if((t=Math.abs(this._33))>m){m=t;}
return m;};x3dom.fields.SFMatrix4f.prototype.norm1_3x3=function(){var max=Math.abs(this._00)+
Math.abs(this._10)+
Math.abs(this._20);var t=0;if((t=Math.abs(this._01)+
Math.abs(this._11)+
Math.abs(this._21))>max){max=t;}
if((t=Math.abs(this._02)+
Math.abs(this._12)+
Math.abs(this._22))>max){max=t;}
return max;};x3dom.fields.SFMatrix4f.prototype.normInf_3x3=function(){var max=Math.abs(this._00)+
Math.abs(this._01)+
Math.abs(this._02);var t=0;if((t=Math.abs(this._10)+
Math.abs(this._11)+
Math.abs(this._12))>max){max=t;}
if((t=Math.abs(this._20)+
Math.abs(this._21)+
Math.abs(this._22))>max){max=t;}
return max;};x3dom.fields.SFMatrix4f.prototype.adjointT_3x3=function(){var result=x3dom.fields.SFMatrix4f.identity();result._00=this._11*this._22-this._12*this._21;result._01=this._12*this._20-this._10*this._22;result._02=this._10*this._21-this._11*this._20;result._10=this._21*this._02-this._22*this._01;result._11=this._22*this._00-this._20*this._02;result._12=this._20*this._01-this._21*this._00;result._20=this._01*this._12-this._02*this._11;result._21=this._02*this._10-this._00*this._12;result._22=this._00*this._11-this._01*this._10;return result;};x3dom.fields.SFMatrix4f.prototype.equals=function(that){var eps=0.000000000001;return Math.abs(this._00-that._00)<eps&&Math.abs(this._01-that._01)<eps&&Math.abs(this._02-that._02)<eps&&Math.abs(this._03-that._03)<eps&&Math.abs(this._10-that._10)<eps&&Math.abs(this._11-that._11)<eps&&Math.abs(this._12-that._12)<eps&&Math.abs(this._13-that._13)<eps&&Math.abs(this._20-that._20)<eps&&Math.abs(this._21-that._21)<eps&&Math.abs(this._22-that._22)<eps&&Math.abs(this._23-that._23)<eps&&Math.abs(this._30-that._30)<eps&&Math.abs(this._31-that._31)<eps&&Math.abs(this._32-that._32)<eps&&Math.abs(this._33-that._33)<eps;};x3dom.fields.SFMatrix4f.prototype.getTransform=function(translation,rotation,scaleFactor,scaleOrientation,center)
{var m=null;if(arguments.length>4){m=x3dom.fields.SFMatrix4f.translation(center.negate());m=m.mult(this);var c=x3dom.fields.SFMatrix4f.translation(center);m=m.mult(c);}
else{m=x3dom.fields.SFMatrix4f.copy(this);}
var flip=m.decompose(translation,rotation,scaleFactor,scaleOrientation);scaleFactor.setValues(scaleFactor.multiply(flip));};x3dom.fields.SFMatrix4f.prototype.decompose=function(t,r,s,so)
{var A=x3dom.fields.SFMatrix4f.copy(this);var Q=x3dom.fields.SFMatrix4f.identity(),S=x3dom.fields.SFMatrix4f.identity(),SO=x3dom.fields.SFMatrix4f.identity();t.x=A._03;t.y=A._13;t.z=A._23;A._03=0.0;A._13=0.0;A._23=0.0;A._30=0.0;A._31=0.0;A._32=0.0;var det=A.polarDecompose(Q,S);var f=1.0;if(det<0.0){Q=Q.negate();f=-1.0;}
r.setValue(Q);S.spectralDecompose(SO,s);so.setValue(SO);return f;};x3dom.fields.SFMatrix4f.prototype.polarDecompose=function(Q,S)
{var TOL=0.000000000001;var Mk=this.transpose();var Ek=x3dom.fields.SFMatrix4f.identity();var Mk_one=Mk.norm1_3x3();var Mk_inf=Mk.normInf_3x3();var MkAdjT;var MkAdjT_one,MkAdjT_inf;var Ek_one,Mk_det;do
{MkAdjT=Mk.adjointT_3x3();Mk_det=Mk._00*MkAdjT._00+
Mk._01*MkAdjT._01+
Mk._02*MkAdjT._02;if(Mk_det==0.0)
{x3dom.debug.logWarning("polarDecompose: Mk_det == 0.0");break;}
MkAdjT_one=MkAdjT.norm1_3x3();MkAdjT_inf=MkAdjT.normInf_3x3();var gamma=Math.sqrt(Math.sqrt((MkAdjT_one*MkAdjT_inf)/(Mk_one*Mk_inf))/Math.abs(Mk_det));var g1=0.5*gamma;var g2=0.5/(gamma*Mk_det);Ek.setValues(Mk);Mk=Mk.multiply(g1);Mk=Mk.addScaled(MkAdjT,g2);Ek=Ek.addScaled(Mk,-1.0);Ek_one=Ek.norm1_3x3();Mk_one=Mk.norm1_3x3();Mk_inf=Mk.normInf_3x3();}while(Ek_one>(Mk_one*TOL));Q.setValues(Mk.transpose());S.setValues(Mk.mult(this));for(var i=0;i<3;++i)
{for(var j=i;j<3;++j)
{S['_'+j+i]=0.5*(S['_'+j+i]+S['_'+i+j]);S['_'+i+j]=0.5*(S['_'+j+i]+S['_'+i+j]);}}
return Mk_det;};x3dom.fields.SFMatrix4f.prototype.spectralDecompose=function(SO,k)
{var next=[1,2,0];var maxIterations=20;var diag=[this._00,this._11,this._22];var offDiag=[this._12,this._20,this._01];for(var iter=0;iter<maxIterations;++iter)
{var sm=Math.abs(offDiag[0])+Math.abs(offDiag[1])+Math.abs(offDiag[2]);if(sm==0){break;}
for(var i=2;i>=0;--i)
{var p=next[i];var q=next[p];var absOffDiag=Math.abs(offDiag[i]);var g=100.0*absOffDiag;if(absOffDiag>0.0)
{var t=0,h=diag[q]-diag[p];var absh=Math.abs(h);if(absh+g==absh)
{t=offDiag[i]/h;}
else
{var theta=0.5*h/offDiag[i];t=1.0/(Math.abs(theta)+Math.sqrt(theta*theta+1.0));t=theta<0.0?-t:t;}
var c=1.0/Math.sqrt(t*t+1.0);var s=t*c;var tau=s/(c+1.0);var ta=t*offDiag[i];offDiag[i]=0.0;diag[p]-=ta;diag[q]+=ta;var offDiagq=offDiag[q];offDiag[q]-=s*(offDiag[p]+tau*offDiagq);offDiag[p]+=s*(offDiagq-tau*offDiag[p]);for(var j=2;j>=0;--j)
{var a=SO['_'+j+p];var b=SO['_'+j+q];SO['_'+j+p]-=s*(b+tau*a);SO['_'+j+q]+=s*(a-tau*b);}}}}
k.x=diag[0];k.y=diag[1];k.z=diag[2];};x3dom.fields.SFMatrix4f.prototype.log=function(){var maxiter=12;var eps=1e-12;var A=x3dom.fields.SFMatrix4f.copy(this),Z=x3dom.fields.SFMatrix4f.copy(this);Z._00-=1;Z._11-=1;Z._22-=1;Z._33-=1;var k=0;while(Z.normInfinity()>0.5)
{A=A.sqrt();Z.setValues(A);Z._00-=1;Z._11-=1;Z._22-=1;Z._33-=1;k++;}
A._00-=1;A._11-=1;A._22-=1;A._33-=1;A=A.negate();Z.setValues(A);var result=x3dom.fields.SFMatrix4f.copy(A);var i=1;while(Z.normInfinity()>eps&&i<maxiter)
{Z=Z.mult(A);i++;result=result.addScaled(Z,1.0/i);}
return result.multiply(-(1<<k));};x3dom.fields.SFMatrix4f.prototype.exp=function(){var q=6;var A=x3dom.fields.SFMatrix4f.copy(this),D=x3dom.fields.SFMatrix4f.identity(),N=x3dom.fields.SFMatrix4f.identity(),result=x3dom.fields.SFMatrix4f.identity();var k=0,c=1.0;var j=1.0+parseInt(Math.log(A.normInfinity()/0.693));if(j<0){j=0;}
A=A.multiply(1.0/(1<<j));for(k=1;k<=q;k++)
{c*=(q-k+1)/(k*(2*q-k+1));result=A.mult(result);N=N.addScaled(result,c);if(k%2){D=D.addScaled(result,-c);}
else{D=D.addScaled(result,c);}}
result=D.inverse().mult(N);for(k=0;k<j;k++)
{result=result.mult(result);}
return result;};x3dom.fields.SFMatrix4f.prototype.det3=function(a1,a2,a3,b1,b2,b3,c1,c2,c3){return((a1*b2*c3)+(a2*b3*c1)+(a3*b1*c2)-
(a1*b3*c2)-(a2*b1*c3)-(a3*b2*c1));};x3dom.fields.SFMatrix4f.prototype.det=function(){var a1=this._00;var b1=this._10;var c1=this._20;var d1=this._30;var a2=this._01;var b2=this._11;var c2=this._21;var d2=this._31;var a3=this._02;var b3=this._12;var c3=this._22;var d3=this._32;var a4=this._03;var b4=this._13;var c4=this._23;var d4=this._33;return(a1*this.det3(b2,b3,b4,c2,c3,c4,d2,d3,d4)-
b1*this.det3(a2,a3,a4,c2,c3,c4,d2,d3,d4)+
c1*this.det3(a2,a3,a4,b2,b3,b4,d2,d3,d4)-
d1*this.det3(a2,a3,a4,b2,b3,b4,c2,c3,c4));};x3dom.fields.SFMatrix4f.prototype.inverse=function(){var a1=this._00;var b1=this._10;var c1=this._20;var d1=this._30;var a2=this._01;var b2=this._11;var c2=this._21;var d2=this._31;var a3=this._02;var b3=this._12;var c3=this._22;var d3=this._32;var a4=this._03;var b4=this._13;var c4=this._23;var d4=this._33;var rDet=this.det();if(Math.abs(rDet)<1e-30)
{x3dom.debug.logWarning("Invert matrix: singular matrix, no inverse!");return x3dom.fields.SFMatrix4f.identity();}
rDet=1.0/rDet;return new x3dom.fields.SFMatrix4f(+this.det3(b2,b3,b4,c2,c3,c4,d2,d3,d4)*rDet,-this.det3(a2,a3,a4,c2,c3,c4,d2,d3,d4)*rDet,+this.det3(a2,a3,a4,b2,b3,b4,d2,d3,d4)*rDet,-this.det3(a2,a3,a4,b2,b3,b4,c2,c3,c4)*rDet,-this.det3(b1,b3,b4,c1,c3,c4,d1,d3,d4)*rDet,+this.det3(a1,a3,a4,c1,c3,c4,d1,d3,d4)*rDet,-this.det3(a1,a3,a4,b1,b3,b4,d1,d3,d4)*rDet,+this.det3(a1,a3,a4,b1,b3,b4,c1,c3,c4)*rDet,+this.det3(b1,b2,b4,c1,c2,c4,d1,d2,d4)*rDet,-this.det3(a1,a2,a4,c1,c2,c4,d1,d2,d4)*rDet,+this.det3(a1,a2,a4,b1,b2,b4,d1,d2,d4)*rDet,-this.det3(a1,a2,a4,b1,b2,b4,c1,c2,c4)*rDet,-this.det3(b1,b2,b3,c1,c2,c3,d1,d2,d3)*rDet,+this.det3(a1,a2,a3,c1,c2,c3,d1,d2,d3)*rDet,-this.det3(a1,a2,a3,b1,b2,b3,d1,d2,d3)*rDet,+this.det3(a1,a2,a3,b1,b2,b3,c1,c2,c3)*rDet);};x3dom.fields.SFMatrix4f.prototype.toString=function(){return'[SFMatrix4f \n'+
this._00.toFixed(6)+', '+this._01.toFixed(6)+', '+
this._02.toFixed(6)+', '+this._03.toFixed(6)+', \n'+
this._10.toFixed(6)+', '+this._11.toFixed(6)+', '+
this._12.toFixed(6)+', '+this._13.toFixed(6)+', \n'+
this._20.toFixed(6)+', '+this._21.toFixed(6)+', '+
this._22.toFixed(6)+', '+this._23.toFixed(6)+', \n'+
this._30.toFixed(6)+', '+this._31.toFixed(6)+', '+
this._32.toFixed(6)+', '+this._33.toFixed(6)+']';};x3dom.fields.SFMatrix4f.prototype.setValueByStr=function(str){var needTranspose=false;var val=/matrix.*\((.+)\)/;if(val.exec(str)){str=RegExp.$1;needTranspose=true;}
var arr=Array.map(str.split(/[,\s]+/),function(n){return+n;});if(arr.length>=16)
{if(!needTranspose){this._00=arr[0];this._01=arr[1];this._02=arr[2];this._03=arr[3];this._10=arr[4];this._11=arr[5];this._12=arr[6];this._13=arr[7];this._20=arr[8];this._21=arr[9];this._22=arr[10];this._23=arr[11];this._30=arr[12];this._31=arr[13];this._32=arr[14];this._33=arr[15];}
else{this._00=arr[0];this._01=arr[4];this._02=arr[8];this._03=arr[12];this._10=arr[1];this._11=arr[5];this._12=arr[9];this._13=arr[13];this._20=arr[2];this._21=arr[6];this._22=arr[10];this._23=arr[14];this._30=arr[3];this._31=arr[7];this._32=arr[11];this._33=arr[15];}}
else if(arr.length===6){this._00=arr[0];this._01=arr[1];this._02=0;this._03=arr[4];this._10=arr[2];this._11=arr[3];this._12=0;this._13=arr[5];this._20=0;this._21=0;this._22=1;this._23=0;this._30=0;this._31=0;this._32=0;this._33=1;}
else{x3dom.debug.logWarning("SFMatrix4f - can't parse string: "+str);}
return this;};x3dom.fields.SFVec2f=function(x,y){if(arguments.length===0){this.x=this.y=0;}
else{this.x=x;this.y=y;}};x3dom.fields.SFVec2f.copy=function(v){return new x3dom.fields.SFVec2f(v.x,v.y);};x3dom.fields.SFVec2f.parse=function(str){var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);return new x3dom.fields.SFVec2f(+m[1],+m[2]);};x3dom.fields.SFVec2f.prototype.setValues=function(that){this.x=that.x;this.y=that.y;};x3dom.fields.SFVec2f.prototype.at=function(i){switch(i){case 0:return this.x;case 1:return this.y;default:return this.x;}};x3dom.fields.SFVec2f.prototype.add=function(that){return new x3dom.fields.SFVec2f(this.x+that.x,this.y+that.y);};x3dom.fields.SFVec2f.prototype.subtract=function(that){return new x3dom.fields.SFVec2f(this.x-that.x,this.y-that.y);};x3dom.fields.SFVec2f.prototype.negate=function(){return new x3dom.fields.SFVec2f(-this.x,-this.y);};x3dom.fields.SFVec2f.prototype.dot=function(that){return this.x*that.x+this.y*that.y;};x3dom.fields.SFVec2f.prototype.reflect=function(n){var d2=this.dot(n)*2;return new x3dom.fields.SFVec2f(this.x-d2*n.x,this.y-d2*n.y);};x3dom.fields.SFVec2f.prototype.normalize=function(that){var n=this.length();if(n){n=1.0/n;}
return new x3dom.fields.SFVec2f(this.x*n,this.y*n);};x3dom.fields.SFVec2f.prototype.multComponents=function(that){return new x3dom.fields.SFVec2f(this.x*that.x,this.y*that.y);};x3dom.fields.SFVec2f.prototype.multiply=function(n){return new x3dom.fields.SFVec2f(this.x*n,this.y*n);};x3dom.fields.SFVec2f.prototype.divide=function(n){var denom=n?(1.0/n):1.0;return new x3dom.fields.SFVec2f(this.x*denom,this.y*denom);};x3dom.fields.SFVec2f.prototype.equals=function(that,eps){return Math.abs(this.x-that.x)<eps&&Math.abs(this.y-that.y)<eps;};x3dom.fields.SFVec2f.prototype.length=function(){return Math.sqrt((this.x*this.x)+(this.y*this.y));};x3dom.fields.SFVec2f.prototype.toGL=function(){return[this.x,this.y];};x3dom.fields.SFVec2f.prototype.toString=function(){return"{ x "+this.x+" y "+this.y+" }";};x3dom.fields.SFVec2f.prototype.setValueByStr=function(str){var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);this.x=+m[1];this.y=+m[2];return this;};x3dom.fields.SFVec3f=function(x,y,z){if(arguments.length===0){this.x=this.y=this.z=0;}
else{this.x=x;this.y=y;this.z=z;}};x3dom.fields.SFVec3f.copy=function(v){return new x3dom.fields.SFVec3f(v.x,v.y,v.z);};x3dom.fields.SFVec3f.MIN=function(){return new x3dom.fields.SFVec3f(Number.MIN_VALUE,Number.MIN_VALUE,Number.MIN_VALUE);};x3dom.fields.SFVec3f.MAX=function(){return new x3dom.fields.SFVec3f(Number.MAX_VALUE,Number.MAX_VALUE,Number.MAX_VALUE);};x3dom.fields.SFVec3f.parse=function(str){try{var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);return new x3dom.fields.SFVec3f(+m[1],+m[2],+m[3]);}
catch(e){var c=x3dom.fields.SFColor.colorParse(str);return new x3dom.fields.SFVec3f(c.r,c.g,c.b);}};x3dom.fields.SFVec3f.prototype.setValues=function(that){this.x=that.x;this.y=that.y;this.z=that.z;};x3dom.fields.SFVec3f.prototype.at=function(i){switch(i){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:return this.x;}};x3dom.fields.SFVec3f.prototype.add=function(that){return new x3dom.fields.SFVec3f(this.x+that.x,this.y+that.y,this.z+that.z);};x3dom.fields.SFVec3f.prototype.addScaled=function(that,s){return new x3dom.fields.SFVec3f(this.x+s*that.x,this.y+s*that.y,this.z+s*that.z);};x3dom.fields.SFVec3f.prototype.subtract=function(that){return new x3dom.fields.SFVec3f(this.x-that.x,this.y-that.y,this.z-that.z);};x3dom.fields.SFVec3f.prototype.negate=function(){return new x3dom.fields.SFVec3f(-this.x,-this.y,-this.z);};x3dom.fields.SFVec3f.prototype.dot=function(that){return(this.x*that.x+this.y*that.y+this.z*that.z);};x3dom.fields.SFVec3f.prototype.cross=function(that){return new x3dom.fields.SFVec3f(this.y*that.z-this.z*that.y,this.z*that.x-this.x*that.z,this.x*that.y-this.y*that.x);};x3dom.fields.SFVec3f.prototype.reflect=function(n){var d2=this.dot(n)*2;return new x3dom.fields.SFVec3f(this.x-d2*n.x,this.y-d2*n.y,this.z-d2*n.z);};x3dom.fields.SFVec3f.prototype.length=function(){return Math.sqrt((this.x*this.x)+(this.y*this.y)+(this.z*this.z));};x3dom.fields.SFVec3f.prototype.normalize=function(that){var n=this.length();if(n){n=1.0/n;}
return new x3dom.fields.SFVec3f(this.x*n,this.y*n,this.z*n);};x3dom.fields.SFVec3f.prototype.multComponents=function(that){return new x3dom.fields.SFVec3f(this.x*that.x,this.y*that.y,this.z*that.z);};x3dom.fields.SFVec3f.prototype.multiply=function(n){return new x3dom.fields.SFVec3f(this.x*n,this.y*n,this.z*n);};x3dom.fields.SFVec3f.prototype.divide=function(n){var denom=n?(1.0/n):1.0;return new x3dom.fields.SFVec3f(this.x*denom,this.y*denom,this.z*denom);};x3dom.fields.SFVec3f.prototype.equals=function(that,eps){return Math.abs(this.x-that.x)<eps&&Math.abs(this.y-that.y)<eps&&Math.abs(this.z-that.z)<eps;};x3dom.fields.SFVec3f.prototype.toGL=function(){return[this.x,this.y,this.z];};x3dom.fields.SFVec3f.prototype.toString=function(){return"{ x "+this.x+" y "+this.y+" z "+this.z+" }";};x3dom.fields.SFVec3f.prototype.setValueByStr=function(str){try{var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);this.x=+m[1];this.y=+m[2];this.z=+m[3];}
catch(e){var c=x3dom.fields.SFColor.colorParse(str);this.x=c.r;this.y=c.g;this.z=c.b;}
return this;};x3dom.fields.SFVec4f=function(x,y,z,w){if(arguments.length===0){this.x=this.y=this.z=this.w=0;}
else{this.x=x;this.y=y;this.z=z;this.w=w;}};x3dom.fields.SFVec4f.copy=function(v){return new x3dom.fields.SFVec4f(v.x,v.y,v.z,v.w);};x3dom.fields.SFVec4f.parse=function(str){var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);return new x3dom.fields.SFVec4f(+m[1],+m[2],+m[3],+m[4]);};x3dom.fields.SFVec4f.prototype.setValueByStr=function(str){var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);this.x=+m[1];this.y=+m[2];this.z=+m[3];this.w=+m[4];return this;};x3dom.fields.SFVec4f.prototype.toGL=function(){return[this.x,this.y,this.z,this.w];};x3dom.fields.SFVec4f.prototype.toString=function(){return"{ x "+this.x+" y "+this.y+" z "+this.z+" w "+this.w+" }";};x3dom.fields.Quaternion=function(x,y,z,w){this.x=x;this.y=y;this.z=z;this.w=w;};x3dom.fields.Quaternion.prototype.multiply=function(that){return new x3dom.fields.Quaternion(this.w*that.x+this.x*that.w+this.y*that.z-this.z*that.y,this.w*that.y+this.y*that.w+this.z*that.x-this.x*that.z,this.w*that.z+this.z*that.w+this.x*that.y-this.y*that.x,this.w*that.w-this.x*that.x-this.y*that.y-this.z*that.z);};x3dom.fields.Quaternion.parseAxisAngle=function(str){var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);return x3dom.fields.Quaternion.axisAngle(new x3dom.fields.SFVec3f(+m[1],+m[2],+m[3]),+m[4]);};x3dom.fields.Quaternion.axisAngle=function(axis,a){var t=axis.length();if(t>x3dom.fields.Eps)
{var s=Math.sin(a/2)/t;var c=Math.cos(a/2);return new x3dom.fields.Quaternion(axis.x*s,axis.y*s,axis.z*s,c);}
else
{return new x3dom.fields.Quaternion(0,0,0,1);}};x3dom.fields.Quaternion.prototype.toMatrix=function(){var xx=this.x*this.x;var xy=this.x*this.y;var xz=this.x*this.z;var yy=this.y*this.y;var yz=this.y*this.z;var zz=this.z*this.z;var wx=this.w*this.x;var wy=this.w*this.y;var wz=this.w*this.z;return new x3dom.fields.SFMatrix4f(1-2*(yy+zz),2*(xy-wz),2*(xz+wy),0,2*(xy+wz),1-2*(xx+zz),2*(yz-wx),0,2*(xz-wy),2*(yz+wx),1-2*(xx+yy),0,0,0,0,1);};x3dom.fields.Quaternion.prototype.toAxisAngle=function()
{var x=0,y=0,z=0;var s=0,a=0;var that=this;if(this.w>1)
{that=x3dom.fields.Quaternion.normalize(this);}
a=2*Math.acos(that.w);s=Math.sqrt(1-that.w*that.w);if(s==0)
{x=that.x;y=that.y;z=that.z;}
else
{x=that.x/s;y=that.y/s;z=that.z/s;}
return[new x3dom.fields.SFVec3f(x,y,z),a];};x3dom.fields.Quaternion.prototype.angle=function()
{return 2*Math.acos(this.w);};x3dom.fields.Quaternion.prototype.setValue=function(matrix)
{var tr,s=1;var qt=[0,0,0];var i=0,j=0,k=0;var nxt=[1,2,0];tr=matrix._00+matrix._11+matrix._22;if(tr>0.0)
{s=Math.sqrt(tr+1.0);this.w=s*0.5;s=0.5/s;this.x=(matrix._21-matrix._12)*s;this.y=(matrix._02-matrix._20)*s;this.z=(matrix._10-matrix._01)*s;}
else
{if(matrix._11>matrix._00){i=1;}
else{i=0;}
if(matrix._22>matrix.at(i,i)){i=2;}
j=nxt[i];k=nxt[j];s=Math.sqrt(matrix.at(i,i)-(matrix.at(j,j)+matrix.at(k,k))+1.0);qt[i]=s*0.5;s=0.5/s;this.w=(matrix.at(k,j)-matrix.at(j,k))*s;qt[j]=(matrix.at(j,i)+matrix.at(i,j))*s;qt[k]=(matrix.at(k,i)+matrix.at(i,k))*s;this.x=qt[0];this.y=qt[1];this.z=qt[2];}
if(this.w>1.0||this.w<-1.0)
{var errThreshold=1+(x3dom.fields.Eps*100);if(this.w>errThreshold||this.w<-errThreshold)
{x3dom.debug.logInfo("MatToQuat: BUG: |quat[4]| ("+this.w+") >> 1.0 !");}
if(this.w>1.0){this.w=1.0;}
else{this.w=-1.0;}}};x3dom.fields.Quaternion.prototype.dot=function(that){return this.x*that.x+this.y*that.y+this.z*that.z+this.w*that.w;};x3dom.fields.Quaternion.prototype.add=function(that){return new x3dom.fields.Quaternion(this.x+that.x,this.y+that.y,this.z+that.z,this.w+that.w);};x3dom.fields.Quaternion.prototype.subtract=function(that){return new x3dom.fields.Quaternion(this.x-that.x,this.y-that.y,this.z-that.z,this.w-that.w);};x3dom.fields.Quaternion.prototype.setValues=function(that){this.x=that.x;this.y=that.y;this.z=that.z;this.w=that.w;};x3dom.fields.Quaternion.prototype.equals=function(that,eps){return Math.abs(this.x-that.x)<eps&&Math.abs(this.y-that.y)<eps&&Math.abs(this.z-that.z)<eps&&Math.abs(this.w-that.w)<eps;};x3dom.fields.Quaternion.prototype.multScalar=function(s){return new x3dom.fields.Quaternion(this.x*s,this.y*s,this.z*s,this.w*s);};x3dom.fields.Quaternion.prototype.normalize=function(that){var d2=this.dot(that);var id=1.0;if(d2){id=1.0/Math.sqrt(d2);}
return new x3dom.fields.Quaternion(this.x*id,this.y*id,this.z*id,this.w*id);};x3dom.fields.Quaternion.prototype.negate=function(){return new x3dom.fields.Quaternion(-this.x,-this.y,-this.z,-this.w);};x3dom.fields.Quaternion.prototype.inverse=function(){return new x3dom.fields.Quaternion(-this.x,-this.y,-this.z,this.w);};x3dom.fields.Quaternion.prototype.slerp=function(that,t){var cosom=this.dot(that);var rot1;if(cosom<0.0)
{cosom=-cosom;rot1=that.negate();}
else
{rot1=new x3dom.fields.Quaternion(that.x,that.y,that.z,that.w);}
var scalerot0,scalerot1;if((1.0-cosom)>0.00001)
{var omega=Math.acos(cosom);var sinom=Math.sin(omega);scalerot0=Math.sin((1.0-t)*omega)/sinom;scalerot1=Math.sin(t*omega)/sinom;}
else
{scalerot0=1.0-t;scalerot1=t;}
return this.multScalar(scalerot0).add(rot1.multScalar(scalerot1));};x3dom.fields.Quaternion.rotateFromTo=function(fromVec,toVec){var from=fromVec.normalize();var to=toVec.normalize();var cost=from.dot(to);if(cost>0.99999)
{return new x3dom.fields.Quaternion(0,0,0,1);}
else if(cost<-0.99999)
{var cAxis=new x3dom.fields.SFVec3f(1,0,0);var tmp=from.cross(cAxis);if(tmp.length()<0.00001)
{cAxis.x=0;cAxis.y=1;cAxis.z=0;tmp=from.cross(cAxis);}
tmp=tmp.normalize();return x3dom.fields.Quaternion.axisAngle(tmp,Math.PI);}
var axis=fromVec.cross(toVec);axis=axis.normalize();var s=Math.sqrt(0.5*(1.0-cost));axis=axis.multiply(s);s=Math.sqrt(0.5*(1.0+cost));return new x3dom.fields.Quaternion(axis.x,axis.y,axis.z,s);};x3dom.fields.Quaternion.prototype.toGL=function(){var val=this.toAxisAngle();return[val[0].x,val[0].y,val[0].z,val[1]];};x3dom.fields.Quaternion.prototype.toString=function(){return'(('+this.x+', '+this.y+', '+this.z+'), '+this.w+')';};x3dom.fields.Quaternion.prototype.setValueByStr=function(str){var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);var quat=x3dom.fields.Quaternion.axisAngle(new x3dom.fields.SFVec3f(+m[1],+m[2],+m[3]),+m[4]);this.x=quat.x;this.y=quat.y;this.z=quat.z;this.w=quat.w;return this;};x3dom.fields.SFColor=function(r,g,b){if(arguments.length===0){this.r=this.g=this.b=0;}
else{this.r=r;this.g=g;this.b=b;}};x3dom.fields.SFColor.parse=function(str){try{var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);return new x3dom.fields.SFColor(+m[1],+m[2],+m[3]);}
catch(e){return x3dom.fields.SFColor.colorParse(str);}};x3dom.fields.SFColor.prototype.setHSV=function(h,s,v){x3dom.debug.logWarning("SFColor.setHSV() NYI");};x3dom.fields.SFColor.prototype.getHSV=function(){var h=0,s=0,v=0;x3dom.debug.logWarning("SFColor.getHSV() NYI");return[h,s,v];};x3dom.fields.SFColor.prototype.setValues=function(color){this.r=color.r;this.g=color.g;this.b=color.b;};x3dom.fields.SFColor.prototype.equals=function(that,eps){return Math.abs(this.r-that.r)<eps&&Math.abs(this.g-that.g)<eps&&Math.abs(this.b-that.b)<eps;};x3dom.fields.SFColor.prototype.add=function(that){return new x3dom.fields.SFColor(this.r+that.r,this.g+that.g,this.b+that.b);};x3dom.fields.SFColor.prototype.subtract=function(that){return new x3dom.fields.SFColor(this.r-that.r,this.g-that.g,this.b-that.b);};x3dom.fields.SFColor.prototype.multiply=function(n){return new x3dom.fields.SFColor(this.r*n,this.g*n,this.b*n);};x3dom.fields.SFColor.prototype.toGL=function(){return[this.r,this.g,this.b];};x3dom.fields.SFColor.prototype.toString=function(){return"{ r "+this.r+" g "+this.g+" b "+this.b+" }";};x3dom.fields.SFColor.prototype.setValueByStr=function(str){try{var m=/^\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*$/.exec(str);this.r=+m[1];this.g=+m[2];this.b=+m[3];}
catch(e){var c=x3dom.fields.SFColor.colorParse(str);this.r=c.r;this.g=c.g;this.b=c.b;}
return this;};x3dom.fields.SFColor.colorParse=function(color){var red=0,green=0,blue=0;var color_names={aliceblue:'f0f8ff',antiquewhite:'faebd7',aqua:'00ffff',aquamarine:'7fffd4',azure:'f0ffff',beige:'f5f5dc',bisque:'ffe4c4',black:'000000',blanchedalmond:'ffebcd',blue:'0000ff',blueviolet:'8a2be2',brown:'a52a2a',burlywood:'deb887',cadetblue:'5f9ea0',chartreuse:'7fff00',chocolate:'d2691e',coral:'ff7f50',cornflowerblue:'6495ed',cornsilk:'fff8dc',crimson:'dc143c',cyan:'00ffff',darkblue:'00008b',darkcyan:'008b8b',darkgoldenrod:'b8860b',darkgray:'a9a9a9',darkgreen:'006400',darkkhaki:'bdb76b',darkmagenta:'8b008b',darkolivegreen:'556b2f',darkorange:'ff8c00',darkorchid:'9932cc',darkred:'8b0000',darksalmon:'e9967a',darkseagreen:'8fbc8f',darkslateblue:'483d8b',darkslategray:'2f4f4f',darkturquoise:'00ced1',darkviolet:'9400d3',deeppink:'ff1493',deepskyblue:'00bfff',dimgray:'696969',dodgerblue:'1e90ff',feldspar:'d19275',firebrick:'b22222',floralwhite:'fffaf0',forestgreen:'228b22',fuchsia:'ff00ff',gainsboro:'dcdcdc',ghostwhite:'f8f8ff',gold:'ffd700',goldenrod:'daa520',gray:'808080',green:'008000',greenyellow:'adff2f',honeydew:'f0fff0',hotpink:'ff69b4',indianred:'cd5c5c',indigo:'4b0082',ivory:'fffff0',khaki:'f0e68c',lavender:'e6e6fa',lavenderblush:'fff0f5',lawngreen:'7cfc00',lemonchiffon:'fffacd',lightblue:'add8e6',lightcoral:'f08080',lightcyan:'e0ffff',lightgoldenrodyellow:'fafad2',lightgrey:'d3d3d3',lightgreen:'90ee90',lightpink:'ffb6c1',lightsalmon:'ffa07a',lightseagreen:'20b2aa',lightskyblue:'87cefa',lightslateblue:'8470ff',lightslategray:'778899',lightsteelblue:'b0c4de',lightyellow:'ffffe0',lime:'00ff00',limegreen:'32cd32',linen:'faf0e6',magenta:'ff00ff',maroon:'800000',mediumaquamarine:'66cdaa',mediumblue:'0000cd',mediumorchid:'ba55d3',mediumpurple:'9370d8',mediumseagreen:'3cb371',mediumslateblue:'7b68ee',mediumspringgreen:'00fa9a',mediumturquoise:'48d1cc',mediumvioletred:'c71585',midnightblue:'191970',mintcream:'f5fffa',mistyrose:'ffe4e1',moccasin:'ffe4b5',navajowhite:'ffdead',navy:'000080',oldlace:'fdf5e6',olive:'808000',olivedrab:'6b8e23',orange:'ffa500',orangered:'ff4500',orchid:'da70d6',palegoldenrod:'eee8aa',palegreen:'98fb98',paleturquoise:'afeeee',palevioletred:'d87093',papayawhip:'ffefd5',peachpuff:'ffdab9',peru:'cd853f',pink:'ffc0cb',plum:'dda0dd',powderblue:'b0e0e6',purple:'800080',red:'ff0000',rosybrown:'bc8f8f',royalblue:'4169e1',saddlebrown:'8b4513',salmon:'fa8072',sandybrown:'f4a460',seagreen:'2e8b57',seashell:'fff5ee',sienna:'a0522d',silver:'c0c0c0',skyblue:'87ceeb',slateblue:'6a5acd',slategray:'708090',snow:'fffafa',springgreen:'00ff7f',steelblue:'4682b4',tan:'d2b48c',teal:'008080',thistle:'d8bfd8',tomato:'ff6347',turquoise:'40e0d0',violet:'ee82ee',violetred:'d02090',wheat:'f5deb3',white:'ffffff',whitesmoke:'f5f5f5',yellow:'ffff00',yellowgreen:'9acd32'};if(color_names[color]){color="#"+color_names[color];}
if(color.substr&&color.substr(0,1)==="#"){color=color.substr(1);var len=color.length;if(len===6){red=parseInt("0x"+color.substr(0,2),16)/255.0;green=parseInt("0x"+color.substr(2,2),16)/255.0;blue=parseInt("0x"+color.substr(4,2),16)/255.0;}
else if(len===3){red=parseInt("0x"+color.substr(0,1),16)/15.0;green=parseInt("0x"+color.substr(1,1),16)/15.0;blue=parseInt("0x"+color.substr(2,1),16)/15.0;}}
return new x3dom.fields.SFColor(red,green,blue);};x3dom.fields.SFColorRGBA=function(r,g,b,a){if(arguments.length===0){this.r=this.g=this.b=this.a=0;}
else{this.r=r;this.g=g;this.b=b;this.a=a;}};x3dom.fields.SFColorRGBA.parse=function(str){try{var m=/^([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)$/.exec(str);return new x3dom.fields.SFColorRGBA(+m[1],+m[2],+m[3],+m[4]);}
catch(e){return x3dom.fields.SFColorRGBA.colorParse(str);}};x3dom.fields.SFColorRGBA.prototype.setValues=function(color){this.r=color.r;this.g=color.g;this.b=color.b;this.a=color.a;};x3dom.fields.SFColorRGBA.prototype.equals=function(that,eps){return Math.abs(this.r-that.r)<eps&&Math.abs(this.g-that.g)<eps&&Math.abs(this.b-that.b)<eps&&Math.abs(this.a-that.a)<eps;};x3dom.fields.SFColorRGBA.prototype.toGL=function(){return[this.r,this.g,this.b,this.a];};x3dom.fields.SFColorRGBA.prototype.toString=function(){return"{ r "+this.r+" g "+this.g+" b "+this.b+" a "+this.a+" }";};x3dom.fields.SFColorRGBA.prototype.setValueByStr=function(str){try{var m=/^([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)\s*,?\s*([+\-]?\d*\.*\d*[eE]?[+\-]?\d*?)$/.exec(str);this.r=+m[1];this.g=+m[2];this.b=+m[3];this.a=+m[4];}
catch(e){var c=x3dom.fields.SFColorRGBA.colorParse(str);this.r=c.r;this.g=c.g;this.b=c.b;this.a=c.a;}
return this;};x3dom.fields.SFImage=function(w,h,c,arr){if(arguments.length===0||!(arr&&arr.map)){this.width=this.height=this.comp=0;this.array=[];}
else{this.width=w;this.height=h;this.comp=c;arr.map(function(v){this.array.push(v);},this.array);}};x3dom.fields.SFImage.parse=function(str){var img=new x3dom.fields.SFImage();img.setValueByStr(str);return img;};x3dom.fields.SFImage.prototype.setValueByStr=function(str){var mc=str.match(/(\w+)/g);var n=mc.length;var c2=0;var hex="0123456789ABCDEF";this.array=[];if(n>2){this.width=+mc[0];this.height=+mc[1];this.comp=+mc[2];c2=2*this.comp;}else{this.width=0;this.height=0;this.comp=0;return;}
var len,i;for(i=3;i<n;i++){if(!mc[i].substr){continue;}
if(mc[i].substr(1,1).toLowerCase()!=="x"){var out="";var inp=parseInt(mc[i],10);while(inp!==0){out=hex.charAt(inp%16)+out;inp=inp>>4;}
len=out.length;while(out.length<c2){out="0"+out;}
mc[i]="0x"+out;}
if(mc[i].substr(1,1).toLowerCase()==="x"){mc[i]=mc[i].substr(2);len=mc[i].length;var r,g,b,a;if(len===c2){if(this.comp===1){r=parseInt("0x"+mc[i].substr(0,2),16);this.array.push(r);}
else if(this.comp===2){r=parseInt("0x"+mc[i].substr(0,2),16);g=parseInt("0x"+mc[i].substr(2,2),16);this.array.push(r,g);}
else if(this.comp===3){r=parseInt("0x"+mc[i].substr(0,2),16);g=parseInt("0x"+mc[i].substr(2,2),16);b=parseInt("0x"+mc[i].substr(4,2),16);this.array.push(r,g,b);}
else if(this.comp===4){r=parseInt("0x"+mc[i].substr(0,2),16);g=parseInt("0x"+mc[i].substr(2,2),16);b=parseInt("0x"+mc[i].substr(4,2),16);a=parseInt("0x"+mc[i].substr(6,2),16);this.array.push(r,g,b,a);}}}}};x3dom.fields.SFImage.prototype.toGL=function(){var a=[];Array.map(this.array,function(c){a.push(c);});return a;};x3dom.fields.MFColor=function(colorArray){if(arguments.length===0){}else{var that=this;colorArray.map(function(c){that.push(c);},this);}};x3dom.fields.MFColor.prototype=x3dom.extend([]);x3dom.fields.MFColor.parse=function(str){var mc=str.match(/([+\-0-9eE\.]+)/g);var colors=[];for(var i=0,n=mc?mc.length:0;i<n;i+=3){colors.push(new x3dom.fields.SFColor(+mc[i+0],+mc[i+1],+mc[i+2]));}
return new x3dom.fields.MFColor(colors);};x3dom.fields.MFColor.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-0-9eE\.]+)/g);for(var i=0,n=mc?mc.length:0;i<n;i+=3){this.push(new x3dom.fields.SFColor(+mc[i+0],+mc[i+1],+mc[i+2]));}};x3dom.fields.MFColor.prototype.toGL=function(){var a=[];Array.map(this,function(c){a.push(c.r);a.push(c.g);a.push(c.b);});return a;};x3dom.fields.MFColorRGBA=function(colorArray){if(arguments.length===0){}
else{var that=this;colorArray.map(function(c){that.push(c);},this);}};x3dom.fields.MFColorRGBA.prototype=x3dom.extend([]);x3dom.fields.MFColorRGBA.parse=function(str){var mc=str.match(/([+\-0-9eE\.]+)/g);var colors=[];for(var i=0,n=mc?mc.length:0;i<n;i+=4){colors.push(new x3dom.fields.SFColorRGBA(+mc[i+0],+mc[i+1],+mc[i+2],+mc[i+3]));}
return new x3dom.fields.MFColorRGBA(colors);};x3dom.fields.MFColorRGBA.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-0-9eE\.]+)/g);for(var i=0,n=mc?mc.length:0;i<n;i+=4){this.push(new x3dom.fields.SFColor(+mc[i+0],+mc[i+1],+mc[i+2],+mc[i+3]));}};x3dom.fields.MFColorRGBA.prototype.toGL=function(){var a=[];Array.map(this,function(c){a.push(c.r);a.push(c.g);a.push(c.b);a.push(c.a);});return a;};x3dom.fields.MFRotation=function(rotArray){if(arguments.length===0){}
else{var that=this;rotArray.map(function(v){that.push(v);},this);}};x3dom.fields.MFRotation.prototype=x3dom.extend([]);x3dom.fields.MFRotation.parse=function(str){var mc=str.match(/([+\-0-9eE\.]+)/g);var vecs=[];for(var i=0,n=mc?mc.length:0;i<n;i+=4){vecs.push(x3dom.fields.Quaternion.axisAngle(new x3dom.fields.SFVec3f(+mc[i+0],+mc[i+1],+mc[i+2]),+mc[i+3]));}
return new x3dom.fields.MFRotation(vecs);};x3dom.fields.MFRotation.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-0-9eE\.]+)/g);for(var i=0,n=mc?mc.length:0;i<n;i+=4){this.push(x3dom.fields.Quaternion.axisAngle(new x3dom.fields.SFVec3f(+mc[i+0],+mc[i+1],+mc[i+2]),+mc[i+3]));}};x3dom.fields.MFRotation.prototype.toGL=function(){var a=[];Array.map(this,function(c){var val=c.toAxisAngle();a.push(val[0].x);a.push(val[0].y);a.push(val[0].z);a.push(val[1]);});return a;};x3dom.fields.MFVec3f=function(vec3Array){if(arguments.length===0){}
else{var that=this;vec3Array.map(function(v){that.push(v);},this);}};x3dom.fields.MFVec3f.copy=function(vec3Array){var destination=new x3dom.fields.MFVec3f();vec3Array.map(function(v){destination.push(x3dom.fields.SFVec3f.copy(v));},this);return destination;};x3dom.fields.MFVec3f.prototype=x3dom.extend([]);x3dom.fields.MFVec3f.parse=function(str){var mc=str.match(/([+\-0-9eE\.]+)/g);var vecs=[];for(var i=0,n=mc?mc.length:0;i<n;i+=3){vecs.push(new x3dom.fields.SFVec3f(+mc[i+0],+mc[i+1],+mc[i+2]));}
return new x3dom.fields.MFVec3f(vecs);};x3dom.fields.MFVec3f.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-0-9eE\.]+)/g);for(var i=0,n=mc?mc.length:0;i<n;i+=3){this.push(new x3dom.fields.SFVec3f(+mc[i+0],+mc[i+1],+mc[i+2]));}};x3dom.fields.MFVec3f.prototype.toGL=function(){var a=[];Array.map(this,function(c){a.push(c.x);a.push(c.y);a.push(c.z);});return a;};x3dom.fields.MFVec2f=function(vec2Array){if(arguments.length===0){}
else{var that=this;vec2Array.map(function(v){that.push(v);},this);}};x3dom.fields.MFVec2f.prototype=x3dom.extend([]);x3dom.fields.MFVec2f.parse=function(str){var mc=str.match(/([+\-0-9eE\.]+)/g);var vecs=[];for(var i=0,n=mc?mc.length:0;i<n;i+=2){vecs.push(new x3dom.fields.SFVec2f(+mc[i+0],+mc[i+1]));}
return new x3dom.fields.MFVec2f(vecs);};x3dom.fields.MFVec2f.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-0-9eE\.]+)/g);for(var i=0,n=mc?mc.length:0;i<n;i+=2){this.push(new x3dom.fields.SFVec2f(+mc[i+0],+mc[i+1]));}};x3dom.fields.MFVec2f.prototype.toGL=function(){var a=[];Array.map(this,function(v){a.push(v.x);a.push(v.y);});return a;};x3dom.fields.MFInt32=function(array){if(arguments.length===0){}
else if(array&&array.map){var that=this;array.map(function(v){that.push(v);},this);}};x3dom.fields.MFInt32.prototype=x3dom.extend([]);x3dom.fields.MFInt32.parse=function(str){var mc=str.match(/([+\-]?\d+\s*){1},?\s*/g);var vals=[];for(var i=0,n=mc?mc.length:0;i<n;++i){vals.push(parseInt(mc[i],10));}
return new x3dom.fields.MFInt32(vals);};x3dom.fields.MFInt32.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-]?\d+\s*){1},?\s*/g);for(var i=0,n=mc?mc.length:0;i<n;++i){this.push(parseInt(mc[i],10));}};x3dom.fields.MFInt32.prototype.toGL=function(){var a=[];Array.map(this,function(v){a.push(v);});return a;};x3dom.fields.MFFloat=function(array){if(arguments.length===0){}
else if(array&&array.map){var that=this;array.map(function(v){that.push(v);},this);}};x3dom.fields.MFFloat.prototype=x3dom.extend([]);x3dom.fields.MFFloat.parse=function(str){var mc=str.match(/([+\-0-9eE\.]+)/g);var vals=[];for(var i=0,n=mc?mc.length:0;i<n;i++){vals.push(+mc[i]);}
return new x3dom.fields.MFFloat(vals);};x3dom.fields.MFFloat.prototype.setValueByStr=function(str){while(this.length){this.pop();}
var mc=str.match(/([+\-0-9eE\.]+)/g);for(var i=0,n=mc?mc.length:0;i<n;i++){this.push(+mc[i]);}};x3dom.fields.MFFloat.prototype.toGL=function(){var a=[];Array.map(this,function(v){a.push(v);});return a;};x3dom.fields.MFString=function(strArray){if(arguments.length===0){}
else if(strArray&&strArray.map){var that=this;strArray.map(function(v){that.push(v);},this);}};x3dom.fields.MFString.parse=function(str){var arr=[];if(str.length&&str[0]=='"'){var m,re=/"((?:[^\\"]|\\\\|\\")*)"/g;while((m=re.exec(str))){var s=m[1].replace(/\\([\\"])/,"$1");if(s!==undefined){arr.push(s);}}}
else{arr.push(str);}
return new x3dom.fields.MFString(arr);};x3dom.fields.MFString.prototype=x3dom.extend([]);x3dom.fields.MFString.prototype.setValueByStr=function(str){var arr=this;while(arr.length){arr.pop();}
if(str.length&&str[0]=='"'){var m,re=/"((?:[^\\"]|\\\\|\\")*)"/g;while((m=re.exec(str))){var s=m[1].replace(/\\([\\"])/,"$1");if(s!==undefined){arr.push(s);}}}
else{arr.push(str);}
return this;};x3dom.fields.MFString.prototype.toString=function(){var str="";for(var i=0;i<this.length;i++){str=str+this[i]+" ";}
return str;};x3dom.fields.SFNode=function(type){this.type=type;this.node=null;};x3dom.fields.SFNode.prototype.hasLink=function(node){return(node?(this.node===node):this.node);};x3dom.fields.SFNode.prototype.addLink=function(node){this.node=node;return true;};x3dom.fields.SFNode.prototype.rmLink=function(node){if(this.node===node){this.node=null;return true;}
else{return false;}};x3dom.fields.MFNode=function(type){this.type=type;this.nodes=[];};x3dom.fields.MFNode.prototype.hasLink=function(node){if(node){for(var i=0,n=this.nodes.length;i<n;i++){if(this.nodes[i]===node){return true;}}}
else{return(this.length>0);}
return false;};x3dom.fields.MFNode.prototype.addLink=function(node){this.nodes.push(node);return true;};x3dom.fields.MFNode.prototype.rmLink=function(node){for(var i=0,n=this.nodes.length;i<n;i++){if(this.nodes[i]===node){this.nodes.splice(i,1);return true;}}
return false;};x3dom.fields.MFNode.prototype.length=function(){return this.nodes.length;};x3dom.fields.Line=function(pos,dir)
{if(arguments.length===0)
{this.pos=new x3dom.fields.SFVec3f(0,0,0);this.dir=new x3dom.fields.SFVec3f(0,0,1);}
else
{this.pos=new x3dom.fields.SFVec3f(pos.x,pos.y,pos.z);var n=dir.length();if(n){n=1.0/n;}
this.dir=new x3dom.fields.SFVec3f(dir.x*n,dir.y*n,dir.z*n);}
this.enter=0;this.exit=0;this.hitObject=null;this.hitPoint={};this.dist=Number.MAX_VALUE;};x3dom.fields.Line.prototype.toString=function(){var str='Line: ['+this.pos.toString()+'; '+this.dir.toString()+']';return str;};x3dom.fields.Line.prototype.intersect=function(low,high)
{var isect=0.0;var out=Number.MAX_VALUE;var r,te,tl;if(this.dir.x>x3dom.fields.Eps)
{r=1.0/this.dir.x;te=(low.x-this.pos.x)*r;tl=(high.x-this.pos.x)*r;if(tl<out){out=tl;}
if(te>isect){isect=te;}}
else if(this.dir.x<-x3dom.fields.Eps)
{r=1.0/this.dir.x;te=(high.x-this.pos.x)*r;tl=(low.x-this.pos.x)*r;if(tl<out){out=tl;}
if(te>isect){isect=te;}}
else if(this.pos.x<low.x||this.pos.x>high.x)
{return false;}
if(this.dir.y>x3dom.fields.Eps)
{r=1.0/this.dir.y;te=(low.y-this.pos.y)*r;tl=(high.y-this.pos.y)*r;if(tl<out){out=tl;}
if(te>isect){isect=te;}
if(isect-out>=x3dom.fields.Eps){return false;}}
else if(this.dir.y<-x3dom.fields.Eps)
{r=1.0/this.dir.y;te=(high.y-this.pos.y)*r;tl=(low.y-this.pos.y)*r;if(tl<out){out=tl;}
if(te>isect){isect=te;}
if(isect-out>=x3dom.fields.Eps){return false;}}
else if(this.pos.y<low.y||this.pos.y>high.y)
{return false;}
if(this.dir.z>x3dom.fields.Eps)
{r=1.0/this.dir.z;te=(low.z-this.pos.z)*r;tl=(high.z-this.pos.z)*r;if(tl<out){out=tl;}
if(te>isect){isect=te;}}
else if(this.dir.z<-x3dom.fields.Eps)
{r=1.0/this.dir.z;te=(high.z-this.pos.z)*r;tl=(low.z-this.pos.z)*r;if(tl<out){out=tl;}
if(te>isect){isect=te;}}
else if(this.pos.z<low.z||this.pos.z>high.z)
{return false;}
this.enter=isect;this.exit=out;return(isect-out<x3dom.fields.Eps);};x3dom.fields.BoxVolume=function(min,max)
{if(arguments.length<2){this.min=new x3dom.fields.SFVec3f(0,0,0);this.max=new x3dom.fields.SFVec3f(0,0,0);this.valid=false;}
else{this.min=min;this.max=max;this.valid=true;}};x3dom.fields.BoxVolume.prototype.setBounds=function(min,max)
{this.min.setValues(min);this.max.setValues(max);this.valid=true;};x3dom.fields.BoxVolume.prototype.setBoundsByCenterSize=function(center,size)
{var halfSize=size.multiply(0.5);this.min=center.subtract(halfSize);this.max=center.add(halfSize);this.valid=true;};x3dom.fields.BoxVolume.prototype.extendBounds=function(min,max)
{if(this.valid)
{if(this.min.x>min.x){this.min.x=min.x;}
if(this.min.y>min.y){this.min.y=min.y;}
if(this.min.z>min.z){this.min.z=min.z;}
if(this.max.x<max.x){this.max.x=max.x;}
if(this.max.y<max.y){this.max.y=max.y;}
if(this.max.z<max.z){this.max.z=max.z;}}
else
{this.setBounds(min,max);}};x3dom.fields.BoxVolume.prototype.getBounds=function(min,max)
{min.setValues(this.min);max.setValues(this.max);};x3dom.fields.BoxVolume.prototype.invalidate=function()
{this.valid=false;};x3dom.fields.BoxVolume.prototype.isValid=function()
{return this.valid;};x3dom.fields.BoxVolume.prototype.getCenter=function()
{return(this.min.add(this.max)).multiply(0.5);};x3dom.fields.BoxVolume.prototype.getDiameter=function()
{return this.max.subtract(this.min).length();};x3dom.fields.BoxVolume.prototype.transform=function(m)
{var xmin,ymin,zmin;var xmax,ymax,zmax;xmin=xmax=m._03;ymin=ymax=m._13;zmin=zmax=m._23;var a=this.max.x*m._00;var b=this.min.x*m._00;if(a>=b){xmax+=a;xmin+=b;}
else{xmax+=b;xmin+=a;}
a=this.max.y*m._01;b=this.min.y*m._01;if(a>=b){xmax+=a;xmin+=b;}
else{xmax+=b;xmin+=a;}
a=this.max.z*m._02;b=this.min.z*m._02;if(a>=b){xmax+=a;xmin+=b;}
else{xmax+=b;xmin+=a;}
a=this.max.x*m._10;b=this.min.x*m._10;if(a>=b){ymax+=a;ymin+=b;}
else{ymax+=b;ymin+=a;}
a=this.max.y*m._11;b=this.min.y*m._11;if(a>=b){ymax+=a;ymin+=b;}
else{ymax+=b;ymin+=a;}
a=this.max.z*m._12;b=this.min.z*m._12;if(a>=b){ymax+=a;ymin+=b;}
else{ymax+=b;ymin+=a;}
a=this.max.x*m._20;b=this.min.x*m._20;if(a>=b){zmax+=a;zmin+=b;}
else{zmax+=b;zmin+=a;}
a=this.max.y*m._21;b=this.min.y*m._21;if(a>=b){zmax+=a;zmin+=b;}
else{zmax+=b;zmin+=a;}
a=this.max.z*m._22;b=this.min.z*m._22;if(a>=b){zmax+=a;zmin+=b;}
else{zmax+=b;zmin+=a;}
this.min.x=xmin;this.min.y=ymin;this.min.z=zmin;this.max.x=xmax;this.max.y=ymax;this.max.z=zmax;};x3dom.fields.FrustumVolume=function(clipMat)
{this.planeNormals=[];this.planeDistances=[];this.directionIndex=[];if(arguments.length===0){return;}
var planeEquation=[];for(var i=0;i<6;i++){this.planeNormals[i]=new x3dom.fields.SFVec3f(0,0,0);this.planeDistances[i]=0;this.directionIndex[i]=0;planeEquation[i]=new x3dom.fields.SFVec4f(0,0,0,0);}
planeEquation[0].x=clipMat._30-clipMat._00;planeEquation[0].y=clipMat._31-clipMat._01;planeEquation[0].z=clipMat._32-clipMat._02;planeEquation[0].w=clipMat._33-clipMat._03;planeEquation[1].x=clipMat._30+clipMat._00;planeEquation[1].y=clipMat._31+clipMat._01;planeEquation[1].z=clipMat._32+clipMat._02;planeEquation[1].w=clipMat._33+clipMat._03;planeEquation[2].x=clipMat._30+clipMat._10;planeEquation[2].y=clipMat._31+clipMat._11;planeEquation[2].z=clipMat._32+clipMat._12;planeEquation[2].w=clipMat._33+clipMat._13;planeEquation[3].x=clipMat._30-clipMat._10;planeEquation[3].y=clipMat._31-clipMat._11;planeEquation[3].z=clipMat._32-clipMat._12;planeEquation[3].w=clipMat._33-clipMat._13;planeEquation[4].x=clipMat._30+clipMat._20;planeEquation[4].y=clipMat._31+clipMat._21;planeEquation[4].z=clipMat._32+clipMat._22;planeEquation[4].w=clipMat._33+clipMat._23;planeEquation[5].x=clipMat._30-clipMat._20;planeEquation[5].y=clipMat._31-clipMat._21;planeEquation[5].z=clipMat._32-clipMat._22;planeEquation[5].w=clipMat._33-clipMat._23;for(i=0;i<6;i++){var vectorLength=Math.sqrt(planeEquation[i].x*planeEquation[i].x+
planeEquation[i].y*planeEquation[i].y+
planeEquation[i].z*planeEquation[i].z);planeEquation[i].x/=vectorLength;planeEquation[i].y/=vectorLength;planeEquation[i].z/=vectorLength;planeEquation[i].w/=-vectorLength;}
var updateDirectionIndex=function(normalVec){var ind=0;if(normalVec.x>0)ind|=1;if(normalVec.y>0)ind|=2;if(normalVec.z>0)ind|=4;return ind;};this.planeNormals[3].setValues(planeEquation[0]);this.planeDistances[3]=planeEquation[0].w;this.directionIndex[3]=updateDirectionIndex(this.planeNormals[3]);this.planeNormals[2].setValues(planeEquation[1]);this.planeDistances[2]=planeEquation[1].w;this.directionIndex[2]=updateDirectionIndex(this.planeNormals[2]);this.planeNormals[5].setValues(planeEquation[2]);this.planeDistances[5]=planeEquation[2].w;this.directionIndex[5]=updateDirectionIndex(this.planeNormals[5]);this.planeNormals[4].setValues(planeEquation[3]);this.planeDistances[4]=planeEquation[3].w;this.directionIndex[4]=updateDirectionIndex(this.planeNormals[4]);this.planeNormals[0].setValues(planeEquation[4]);this.planeDistances[0]=planeEquation[4].w;this.directionIndex[0]=updateDirectionIndex(this.planeNormals[0]);this.planeNormals[1].setValues(planeEquation[5]);this.planeDistances[1]=planeEquation[5].w;this.directionIndex[1]=updateDirectionIndex(this.planeNormals[1]);};x3dom.fields.FrustumVolume.prototype.intersect=function(vol)
{if(this.planeNormals.length<6){x3dom.debug.logWarning("FrustumVolume not initialized!");return false;}
var that=this;var min=vol.min,max=vol.max;var setDirectionIndexPoint=function(index){var pnt=new x3dom.fields.SFVec3f(0,0,0);if(index&1){pnt.x=min.x;}
else{pnt.x=max.x;}
if(index&2){pnt.y=min.y;}
else{pnt.y=max.y;}
if(index&4){pnt.z=min.z;}
else{pnt.z=max.z;}
return pnt;};var isInHalfSpace=function(i,pnt){var s=that.planeNormals[i].dot(pnt)-that.planeDistances[i];return(s>=0);};var isOutHalfSpace=function(i){var p=setDirectionIndexPoint(that.directionIndex[i]^7);return!isInHalfSpace(i,p);};for(var i=0;i<6;i++){if(isOutHalfSpace(i))
return false;}
return true;};x3dom.NodeNameSpace=function(name,document){this.name=name;this.doc=document;this.baseURL="";this.defMap={};this.parent=null;this.childSpaces=[];};x3dom.NodeNameSpace.prototype.addNode=function(node,name){this.defMap[name]=node;node._nameSpace=this;};x3dom.NodeNameSpace.prototype.removeNode=function(name){var node=this.defMap.name;delete this.defMap.name;if(node){node._nameSpace=null;}};x3dom.NodeNameSpace.prototype.getNamedNode=function(name){return this.defMap[name];};x3dom.NodeNameSpace.prototype.getNamedElement=function(name){var node=this.defMap[name];return(node?node._xmlNode:null);};x3dom.NodeNameSpace.prototype.addSpace=function(space){this.childSpaces.push(space);space.parent=this;};x3dom.NodeNameSpace.prototype.removeSpace=function(space){this.childSpaces.push(space);space.parent=null;};x3dom.NodeNameSpace.prototype.setBaseURL=function(url){var i=url.lastIndexOf("/");this.baseURL=(i>=0)?url.substr(0,i+1):"";x3dom.debug.logInfo("setBaseURL: "+this.baseURL);};x3dom.NodeNameSpace.prototype.getURL=function(url){if(url===undefined||!url.length){return"";}
else{return((url[0]==='/')||(url.indexOf(":")>=0))?url:(this.baseURL+url);}};x3dom.getElementAttribute=function(attrName)
{var attrib=this.__getAttribute(attrName);if((attrib!==undefined)||!this._x3domNode)
return attrib;else
return this._x3domNode._vf[attrName];};x3dom.setElementAttribute=function(attrName,newVal)
{this.__setAttribute(attrName,newVal);this._x3domNode.updateField(attrName,newVal);this._x3domNode._nameSpace.doc.needRender=true;};x3dom.NodeNameSpace.prototype.setupTree=function(domNode){var n=null;if(x3dom.isX3DElement(domNode)){if(domNode._x3domNode){x3dom.debug.logWarning('Tree is already initialized');return null;}
if((domNode.tagName!==undefined)&&(!domNode.__addEventListener)&&(!domNode.__removeEventListener))
{domNode.__addEventListener=domNode.addEventListener;domNode.addEventListener=function(type,func,phase){if(!this._x3domNode._listeners[type]){this._x3domNode._listeners[type]=[];}
this._x3domNode._listeners[type].push(func);this.__addEventListener(type,func,phase);};domNode.__removeEventListener=domNode.removeEventListener;domNode.removeEventListener=function(type,func,phase){var list=this._x3domNode._listeners[type];if(list){for(var it=0;it<list.length;it++){if(list[it]==func){list.splice(it,1);}}}
this.__removeEventListener(type,func,phase);};}
if(domNode.hasAttribute('USE')){n=this.defMap[domNode.getAttribute('USE')];if(!n){n=null;x3dom.debug.logWarning('Could not USE: '+domNode.getAttribute('USE'));}
return n;}
else{if(domNode.localName.toLowerCase()==='route'){var route=domNode;var fromNode=this.defMap[route.getAttribute('fromNode')];var toNode=this.defMap[route.getAttribute('toNode')];if(!(fromNode&&toNode)){x3dom.debug.logWarning("Broken route - can't find all DEFs for "+
route.getAttribute('fromNode')+" -> "+route.getAttribute('toNode'));return null;}
fromNode.setupRoute(route.getAttribute('fromField'),toNode,route.getAttribute('toField'));return null;}
var nodeType=x3dom.nodeTypesLC[domNode.localName.toLowerCase()];if(nodeType===undefined){x3dom.debug.logWarning("Unrecognised X3D element &lt;"+domNode.localName+"&gt;.");}
else{var ctx={doc:this.doc,xmlNode:domNode,nameSpace:this};n=new nodeType(ctx);if((x3dom.userAgentFeature.supportsDOMAttrModified===false)&&(domNode instanceof Element))
{if(domNode.setAttribute&&!domNode.__setAttribute)
{domNode.__setAttribute=domNode.setAttribute;domNode.setAttribute=x3dom.setElementAttribute;}
if(domNode.getAttribute&&!domNode.__getAttribute)
{domNode.__getAttribute=domNode.getAttribute;domNode.getAttribute=x3dom.getElementAttribute;}}
if(domNode.hasAttribute('DEF')){n._DEF=domNode.getAttribute('DEF');this.defMap[n._DEF]=n;}
else{if(domNode.hasAttribute('id')){n._DEF=domNode.getAttribute('id');this.defMap[n._DEF]=n;}}
if(domNode.highlight===undefined)
{domNode.highlight=function(enable,colorStr){var color=x3dom.fields.SFColor.parse(colorStr);this._x3domNode.highlight(enable,color);this._x3domNode._nameSpace.doc.needRender=true;};}
n._xmlNode=domNode;domNode._x3domNode=n;var that=this;Array.forEach(domNode.childNodes,function(childDomNode){var c=that.setupTree(childDomNode);if(c){n.addChild(c,childDomNode.getAttribute("containerField"));}});n.nodeChanged();return n;}}}
else if(domNode.localName){x3dom.debug.logWarning("Unrecognised X3D element &lt;"+domNode.localName+"&gt;.");n=null;}
return n;};x3dom.registerNodeType("X3DNode","Core",defineClass(null,function(ctx){this._DEF=null;this._nameSpace=(ctx&&ctx.nameSpace)?ctx.nameSpace:null;this._vf={};this._vfFieldTypes={};this._cf={};this._cfFieldTypes={};this._fieldWatchers={};this._parentNodes=[];this._listeners={};this._childNodes=[];this.addField_SFNode('metadata',x3dom.nodeTypes.X3DMetadataObject);},{type:function(){return this.constructor;},typeName:function(){return this.constructor._typeName;},addChild:function(node,containerFieldName){if(node){var field=null;if(containerFieldName){field=this._cf[containerFieldName];}
else{for(var fieldName in this._cf){if(this._cf.hasOwnProperty(fieldName)){var testField=this._cf[fieldName];if(x3dom.isa(node,testField.type)){field=testField;break;}}}}
if(field&&field.addLink(node)){node._parentNodes.push(this);this._childNodes.push(node);node.parentAdded(this);return true;}}
return false;},removeChild:function(node){if(node){for(var fieldName in this._cf){if(this._cf.hasOwnProperty(fieldName)){var field=this._cf[fieldName];if(field.rmLink(node)){for(var i=0,n=node._parentNodes.length;i<n;i++){if(node._parentNodes[i]===this){node._parentNodes.splice(i,1);node.parentRemoved(this);}}
for(var j=0,m=this._childNodes.length;j<m;j++){if(this._childNodes[j]===node){this._childNodes.splice(j,1);return true;}}}}}}
return false;},parentAdded:function(parent){},parentRemoved:function(parent){for(var i=0,n=this._childNodes.length;i<n;i++){if(this._childNodes[i]){this._childNodes[i].parentRemoved(this);}}},getCurrentTransform:function(){if(this._parentNodes.length>=1){return this.transformMatrix(this._parentNodes[0].getCurrentTransform());}
else{return x3dom.fields.SFMatrix4f.identity();}},transformMatrix:function(transform){return transform;},getVolume:function(min,max){return false;},invalidateVolume:function(){},volumeValid:function(){return false;},collectDrawableObjects:function(transform,out){},highlight:function(enable,color)
{if(this._vf.hasOwnProperty("diffuseColor"))
{if(enable){if(this._actDiffuseColor===undefined){this._actDiffuseColor=new x3dom.fields.SFColor();this._highlightOn=false;}
if(!this._highlightOn){this._actDiffuseColor.setValues(this._vf.diffuseColor);this._vf.diffuseColor.setValues(color);this._highlightOn=true;}}
else{if(this._actDiffuseColor!==undefined){this._vf.diffuseColor.setValues(this._actDiffuseColor);this._highlightOn=false;}}}
for(var i=0;i<this._childNodes.length;i++)
{if(this._childNodes[i])
this._childNodes[i].highlight(enable,color);}},find:function(type){for(var i=0;i<this._childNodes.length;i++){if(this._childNodes[i]){if(this._childNodes[i].constructor==type){return this._childNodes[i];}
var c=this._childNodes[i].find(type);if(c){return c;}}}
return null;},findAll:function(type){var found=[];for(var i=0;i<this._childNodes.length;i++){if(this._childNodes[i]){if(this._childNodes[i].constructor==type){found.push(this._childNodes[i]);}
found=found.concat(this._childNodes[i].findAll(type));}}
return found;},findParentProperty:function(propertyName,checkDOMNode){var value=this[propertyName];if(!value&&checkDOMNode&&this._xmlNode){value=this._xmlNode[propertyName];}
if(!value){for(var i=0,n=this._parentNodes.length;i<n;i++){if((value=this._parentNodes[i].findParentProperty(propertyName,checkDOMNode))){break;}}}
return value;},findX3DDoc:function(){return this._nameSpace.doc;},doIntersect:function(line){var isect=false;for(var i=0;i<this._childNodes.length;i++){if(this._childNodes[i]){isect=this._childNodes[i].doIntersect(line)||isect;}}
return isect;},postMessage:function(field,msg){this._vf[field]=msg;var listeners=this._fieldWatchers[field];var thisp=this;if(listeners){Array.forEach(listeners,function(l){l.call(thisp,msg);});}},updateField:function(field,msg){var f=this._vf[field];if(f===undefined){var pre="set_";if(field.indexOf(pre)==0){var fieldName=field.substr(pre.length,field.length-1);if(this._vf[fieldName]!==undefined){field=fieldName;f=this._vf[field];}}
if(f===undefined){f={};this._vf[field]=f;}}
if(f!==null){try{this._vf[field].setValueByStr(msg);}
catch(exc1){try{switch((typeof(this._vf[field])).toString()){case"number":if(typeof(msg)=="number")
this._vf[field]=msg;else
this._vf[field]=+msg;break;case"boolean":if(typeof(msg)=="boolean")
this._vf[field]=msg;else
this._vf[field]=(msg.toLowerCase()=="true");break;case"string":this._vf[field]=msg;break;}}
catch(exc2){x3dom.debug.logError("updateField: setValueByStr() NYI for "+typeof(f));}}
this.fieldChanged(field);}},setupRoute:function(fromField,toNode,toField){var pos;var fieldName;var pre="set_",post="_changed";if(!this._vf[fromField]){pos=fromField.indexOf(pre);if(pos===0){fieldName=fromField.substr(pre.length,fromField.length-1);if(this._vf[fieldName]){fromField=fieldName;}}else{pos=fromField.indexOf(post);if(pos>0){fieldName=fromField.substr(0,fromField.length-post.length);if(this._vf[fieldName]){fromField=fieldName;}}}}
if(!toNode._vf[toField]){pos=toField.indexOf(pre);if(pos===0){fieldName=toField.substr(pre.length,toField.length-1);if(toNode._vf[fieldName]){toField=fieldName;}}
else{pos=toField.indexOf(post);if(pos>0){fieldName=toField.substr(0,toField.length-post.length);if(toNode._vf[fieldName]){toField=fieldName;}}}}
if(!this._fieldWatchers[fromField]){this._fieldWatchers[fromField]=[];}
this._fieldWatchers[fromField].push(function(msg){toNode.postMessage(toField,msg);});if(!toNode._fieldWatchers[toField]){toNode._fieldWatchers[toField]=[];}
toNode._fieldWatchers[toField].push(function(msg){toNode._vf[toField]=msg;toNode.fieldChanged(toField);});},fieldChanged:function(fieldName){},nodeChanged:function(){},callEvtHandler:function(eventType,event){var node=this;try{var attrib=node._xmlNode[eventType];event.target=node._xmlNode;if(typeof(attrib)==="function"){attrib.call(node._xmlNode,event);}
else{var funcStr=node._xmlNode.getAttribute(eventType);var func=new Function('event',funcStr);func.call(node._xmlNode,event);}
var list=node._listeners[event.type];if(list){for(var it=0;it<list.length;it++){list[it].call(node._xmlNode,event);}}}
catch(ex){x3dom.debug.logException(ex);}
return event.cancelBubble;},initSetter:function(xmlNode,name){if(xmlNode.__defineSetter__!==undefined){xmlNode.__defineSetter__(name,function(value){xmlNode.setAttribute(name,value);});}
else{Object.defineProperty(xmlNode,name,{set:function(value){xmlNode.setAttribute(name,value);}});}
if(!xmlNode.attributes[name]){if(this._vf[name]){var str="";try{if(this._vf[name].toGL)
str=this._vf[name].toGL().toString();else
str=this._vf[name].toString();}
catch(e){str=this._vf[name].toString();}
if(!str){str="";}
xmlNode.setAttribute(name,str);}}},addField_SFInt32:function(ctx,name,n){this._vf[name]=ctx&&ctx.xmlNode.hasAttribute(name)?parseInt(ctx.xmlNode.getAttribute(name),10):n;if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFInt32";},addField_SFFloat:function(ctx,name,n){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?+ctx.xmlNode.getAttribute(name):n;if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFFloat";},addField_SFDouble:function(ctx,name,n){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?+ctx.xmlNode.getAttribute(name):n;if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFDouble";},addField_SFTime:function(ctx,name,n){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?+ctx.xmlNode.getAttribute(name):n;if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFTime";},addField_SFBool:function(ctx,name,n){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?ctx.xmlNode.getAttribute(name).toLowerCase()==="true":n;if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFBool";},addField_SFString:function(ctx,name,n){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?ctx.xmlNode.getAttribute(name):n;if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFString";},addField_SFColor:function(ctx,name,r,g,b){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.SFColor.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.SFColor(r,g,b);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFColor";},addField_SFColorRGBA:function(ctx,name,r,g,b,a){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.SFColorRGBA.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.SFColorRGBA(r,g,b,a);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFColorRGBA";},addField_SFVec2f:function(ctx,name,x,y){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.SFVec2f.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.SFVec2f(x,y);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFVec2f";},addField_SFVec3f:function(ctx,name,x,y,z){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.SFVec3f.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.SFVec3f(x,y,z);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFVec3f";},addField_SFVec3d:function(ctx,name,x,y,z){this.addField_SFVec3f(ctx,name,x,y,z);this._vfFieldTypes[name]="SFVec3d";},addField_SFRotation:function(ctx,name,x,y,z,a){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.Quaternion.parseAxisAngle(ctx.xmlNode.getAttribute(name)):x3dom.fields.Quaternion.axisAngle(new x3dom.fields.SFVec3f(x,y,z),a);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFRotation";},addField_SFMatrix4f:function(ctx,name,_00,_01,_02,_03,_10,_11,_12,_13,_20,_21,_22,_23,_30,_31,_32,_33){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.SFMatrix4f.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.SFMatrix4f(_00,_01,_02,_03,_10,_11,_12,_13,_20,_21,_22,_23,_30,_31,_32,_33);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFMatrix4f";},addField_SFImage:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.SFImage.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.SFImage(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="SFImage";},addField_MFString:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFString.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFString(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFString";},addField_MFInt32:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFInt32.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFInt32(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFInt32";},addField_MFFloat:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFFloat.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFFloat(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFFloat";},addField_MFDouble:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFFloat.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFFloat(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFDouble";},addField_MFColor:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFColor.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFColor(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFColor";},addField_MFColorRGBA:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFColorRGBA.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFColorRGBA(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFColorRGBA";},addField_MFVec2f:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFVec2f.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFVec2f(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFVec2f";},addField_MFVec3f:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFVec3f.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFVec3f(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFVec3f";},addField_MFVec3d:function(ctx,name,def){this.addField_MFVec3f(ctx,name,def);this._vfFieldTypes[name]="MFVec3d";},addField_MFRotation:function(ctx,name,def){this._vf[name]=ctx&&ctx.xmlNode&&ctx.xmlNode.hasAttribute(name)?x3dom.fields.MFRotation.parse(ctx.xmlNode.getAttribute(name)):new x3dom.fields.MFRotation(def);if(ctx&&ctx.xmlNode){this.initSetter(ctx.xmlNode,name);}
this._vfFieldTypes[name]="MFRotation";},addField_SFNode:function(name,type){this._cf[name]=new x3dom.fields.SFNode(type);this._cfFieldTypes[name]="SFNode";},addField_MFNode:function(name,type){this._cf[name]=new x3dom.fields.MFNode(type);this._cfFieldTypes[name]="MFNode";}}));x3dom.registerNodeType("X3DMetadataObject","Core",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DMetadataObject.superClass.call(this,ctx);this.addField_SFString(ctx,'name',"");this.addField_SFString(ctx,'reference',"");}));x3dom.registerNodeType("MetadataDouble","Core",defineClass(x3dom.nodeTypes.X3DMetadataObject,function(ctx){x3dom.nodeTypes.MetadataDouble.superClass.call(this,ctx);this.addField_MFDouble(ctx,'value',[]);}));x3dom.registerNodeType("MetadataFloat","Core",defineClass(x3dom.nodeTypes.X3DMetadataObject,function(ctx){x3dom.nodeTypes.MetadataFloat.superClass.call(this,ctx);this.addField_MFFloat(ctx,'value',[]);}));x3dom.registerNodeType("MetadataInteger","Core",defineClass(x3dom.nodeTypes.X3DMetadataObject,function(ctx){x3dom.nodeTypes.MetadataInteger.superClass.call(this,ctx);this.addField_MFInt32(ctx,'value',[]);}));x3dom.registerNodeType("MetadataSet","Core",defineClass(x3dom.nodeTypes.X3DMetadataObject,function(ctx){x3dom.nodeTypes.MetadataSet.superClass.call(this,ctx);this.addField_MFNode('value',x3dom.nodeTypes.X3DMetadataObject);}));x3dom.registerNodeType("MetadataString","Core",defineClass(x3dom.nodeTypes.X3DMetadataObject,function(ctx){x3dom.nodeTypes.MetadataString.superClass.call(this,ctx);this.addField_MFString(ctx,'value',[]);}));x3dom.registerNodeType("Field","Core",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.Field.superClass.call(this,ctx);this.addField_SFString(ctx,'name',"");this.addField_SFString(ctx,'type',"");this.addField_SFString(ctx,'value',"");},{fieldChanged:function(fieldName){var that=this;if(fieldName==='value'){Array.forEach(this._parentNodes,function(node){node.fieldChanged(that._vf.name);});}}}));x3dom.registerNodeType("X3DChildNode","Core",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DChildNode.superClass.call(this,ctx);}));x3dom.registerNodeType("X3DBindableNode","Core",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DBindableNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'bind',false);this.addField_SFString(ctx,'description',"");this.addField_SFBool(ctx,'isActive',false);this._autoGen=(ctx&&ctx.autoGen?true:false);if(this._autoGen)
this._vf.description="default"+this.constructor.superClass._typeName;this._stack=null;},{bind:function(value){if(this._stack){if(value){this._stack.push(this);}
else{this._stack.pop(this);}}
else{x3dom.debug.logError('No BindStack in '+this.typeName()+'Bindable');}},activate:function(prev){this.postMessage('isActive',true);x3dom.debug.logInfo('activate '+this.typeName()+'Bindable '+
this._DEF+'/'+this._vf.description);},deactivate:function(prev){this.postMessage('isActive',false);x3dom.debug.logInfo('deactivate '+this.typeName()+'Bindable '+
this._DEF+'/'+this._vf.description);},fieldChanged:function(fieldName){if(fieldName.indexOf("bind")>=0){this.bind(this._vf.bind);}},nodeChanged:function(){this._stack=this._nameSpace.doc._bindableBag.addBindable(this);}}));x3dom.registerNodeType("X3DInfoNode","Core",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DInfoNode.superClass.call(this,ctx);}));x3dom.registerNodeType("WorldInfo","Core",defineClass(x3dom.nodeTypes.X3DInfoNode,function(ctx){x3dom.nodeTypes.WorldInfo.superClass.call(this,ctx);this.addField_MFString(ctx,'info',[]);this.addField_SFString(ctx,'title',"");x3dom.debug.logInfo(this._vf.info);x3dom.debug.logInfo(this._vf.title);}));x3dom.registerNodeType("X3DBoundedNode","Core",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DBoundedNode.superClass.call(this,ctx);this._graph={singlePath:true,localMatrix:null,globalMatrix:null,volume:new x3dom.fields.BoxVolume()};},{getVolume:function(min,max)
{var valid=false;var vol=this._graph.volume;{for(var i=0,n=this._childNodes.length;i<n;i++)
{var child=this._childNodes[i];if(!child)
continue;var childMin=x3dom.fields.SFVec3f.MAX();var childMax=x3dom.fields.SFVec3f.MIN();if(child.getVolume(childMin,childMax))
{vol.extendBounds(childMin,childMax);valid=true;}}
if(valid)
vol.getBounds(min,max);}
return valid;},invalidateVolume:function()
{this._graph.volume.invalidate();Array.forEach(this._parentNodes,function(node){if(node.volumeValid())
node.invalidateVolume();});},volumeValid:function()
{return this._graph.volume.isValid();}}));x3dom.registerNodeType("X3DSensorNode","Core",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DSensorNode.superClass.call(this,ctx);}));x3dom.registerNodeType("Param","Core",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.Param.superClass.call(this,ctx);x3dom.debug.logWarning('DEPRECATED: Param element needs to be child of X3D element '
+'[<a href="http://x3dom.org/docs/latest/configuration.html">DOCS</a>]');}));x3dom.registerNodeType("X3DGroupingNode","Grouping",defineClass(x3dom.nodeTypes.X3DBoundedNode,function(ctx){x3dom.nodeTypes.X3DGroupingNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'render',true);this.addField_MFNode('children',x3dom.nodeTypes.X3DChildNode);},{collectDrawableObjects:function(transform,out)
{if(!this._vf.render||!out){return;}
out.cnt++;for(var i=0,n=this._childNodes.length;i<n;i++){var cnode=this._childNodes[i];if(cnode){var childTransform=cnode.transformMatrix(transform);cnode.collectDrawableObjects(childTransform,out);}}}}));x3dom.registerNodeType("Switch","Grouping",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Switch.superClass.call(this,ctx);this.addField_SFInt32(ctx,'whichChoice',-1);},{getVolume:function(min,max)
{if(this._vf.whichChoice<0||this._vf.whichChoice>=this._childNodes.length){return false;}
var child=this._childNodes[this._vf.whichChoice];if(child){return child.getVolume(min,max);}
return false;},find:function(type)
{if(this._vf.whichChoice<0||this._vf.whichChoice>=this._childNodes.length){return null;}
var child=this._childNodes[this._vf.whichChoice];if(child){if(child.constructor==type){return child;}
var c=child.find(type);if(c){return c;}}
return null;},findAll:function(type)
{if(this._vf.whichChoice<0||this._vf.whichChoice>=this._childNodes.length){return[];}
var found=[];var child=this._childNodes[this._vf.whichChoice];if(child){if(child.constructor==type){found.push(child);}
found=found.concat(child.findAll(type));}
return found;},collectDrawableObjects:function(transform,out)
{if(!out||this._vf.whichChoice<0||this._vf.whichChoice>=this._childNodes.length){return;}
out.cnt++;var cnode=this._childNodes[this._vf.whichChoice];if(cnode){var childTransform=cnode.transformMatrix(transform);cnode.collectDrawableObjects(childTransform,out);}},doIntersect:function(line)
{if(this._vf.whichChoice<0||this._vf.whichChoice>=this._childNodes.length){return false;}
var child=this._childNodes[this._vf.whichChoice];if(child){return child.doIntersect(line);}
return false;}}));x3dom.registerNodeType("X3DTransformNode","Grouping",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.X3DTransformNode.superClass.call(this,ctx);if(ctx)
ctx.doc._nodeBag.trans.push(this);else
x3dom.debug.logWarning("X3DTransformNode: No runtime context found!");this._trafo=null;},{tick:function(t)
{if(this._xmlNode&&(this._xmlNode['transform']||this._xmlNode.hasAttribute('transform')||this._listeners['transform']))
{var transMatrix=this.getCurrentTransform();var event={target:{},type:'transform',worldX:transMatrix._03,worldY:transMatrix._13,worldZ:transMatrix._23,stopPropagation:function(){this.cancelBubble=true;}};var attrib=this._xmlNode[event.type];if(typeof(attrib)==="function")
attrib.call(this._xmlNode,event);else
{var funcStr=this._xmlNode.getAttribute(event.type);var func=new Function('event',funcStr);func.call(this._xmlNode,event);}
var list=this._listeners[event.type];if(list)
for(var it=0;it<list.length;it++)
list[it].call(this._xmlNode,event);}
var trans=x3dom.getStyle(this._xmlNode,"-webkit-transform");if(trans&&(trans!='none')){this._trafo.setValueByStr(trans);return true;}
return false;},transformMatrix:function(transform){return transform.mult(this._trafo);},getVolume:function(min,max)
{var valid=false;var vol=this._graph.volume;{for(var i=0,n=this._childNodes.length;i<n;i++)
{var child=this._childNodes[i];if(!child)
continue;var childMin=x3dom.fields.SFVec3f.MAX();var childMax=x3dom.fields.SFVec3f.MIN();if(child.getVolume(childMin,childMax))
{vol.extendBounds(childMin,childMax);valid=true;}}
if(valid)
{vol.transform(this._trafo);vol.getBounds(min,max);vol.invalidate();}}
return valid;},doIntersect:function(line)
{var isect=false;var mat=this._trafo.inverse();var tmpPos=new x3dom.fields.SFVec3f(line.pos.x,line.pos.y,line.pos.z);var tmpDir=new x3dom.fields.SFVec3f(line.dir.x,line.dir.y,line.dir.z);line.pos=mat.multMatrixPnt(line.pos);line.dir=mat.multMatrixVec(line.dir);if(line.hitObject){line.dist*=line.dir.length();}
for(var i=0;i<this._childNodes.length;i++)
{if(this._childNodes[i]){isect=this._childNodes[i].doIntersect(line)||isect;}}
line.pos.setValues(tmpPos);line.dir.setValues(tmpDir);if(isect){line.hitPoint=this._trafo.multMatrixPnt(line.hitPoint);line.dist*=line.dir.length();}
return isect;},parentRemoved:function(parent)
{var i,n;if(this._parentNodes.length===0){var doc=this.findX3DDoc();for(i=0,n=doc._nodeBag.trans.length;i<n;i++){if(doc._nodeBag.trans[i]===this){doc._nodeBag.trans.splice(i,1);}}}
for(i=0,n=this._childNodes.length;i<n;i++){if(this._childNodes[i]){this._childNodes[i].parentRemoved(this);}}}}));x3dom.registerNodeType("Transform","Grouping",defineClass(x3dom.nodeTypes.X3DTransformNode,function(ctx){x3dom.nodeTypes.Transform.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'center',0,0,0);this.addField_SFVec3f(ctx,'translation',0,0,0);this.addField_SFRotation(ctx,'rotation',0,0,1,0);this.addField_SFVec3f(ctx,'scale',1,1,1);this.addField_SFRotation(ctx,'scaleOrientation',0,0,1,0);this._trafo=x3dom.fields.SFMatrix4f.translation(this._vf.translation.add(this._vf.center)).mult(this._vf.rotation.toMatrix()).mult(this._vf.scaleOrientation.toMatrix()).mult(x3dom.fields.SFMatrix4f.scale(this._vf.scale)).mult(this._vf.scaleOrientation.toMatrix().inverse()).mult(x3dom.fields.SFMatrix4f.translation(this._vf.center.negate()));},{fieldChanged:function(fieldName){this._trafo=x3dom.fields.SFMatrix4f.translation(this._vf.translation.add(this._vf.center)).mult(this._vf.rotation.toMatrix()).mult(this._vf.scaleOrientation.toMatrix()).mult(x3dom.fields.SFMatrix4f.scale(this._vf.scale)).mult(this._vf.scaleOrientation.toMatrix().inverse()).mult(x3dom.fields.SFMatrix4f.translation(this._vf.center.negate()));}}));x3dom.registerNodeType("MatrixTransform","Grouping",defineClass(x3dom.nodeTypes.X3DTransformNode,function(ctx){x3dom.nodeTypes.MatrixTransform.superClass.call(this,ctx);this.addField_SFMatrix4f(ctx,'matrix',1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);this._trafo=this._vf.matrix.transpose();},{fieldChanged:function(fieldName){this._trafo=this._vf.matrix.transpose();}}));x3dom.registerNodeType("Group","Grouping",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Group.superClass.call(this,ctx);},{}));x3dom.registerNodeType("StaticGroup","Grouping",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.StaticGroup.superClass.call(this,ctx);x3dom.debug.logWarning("StaticGroup NYI");}));x3dom.registerNodeType("RemoteSelectionGroup","Grouping",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.RemoteSelectionGroup.superClass.call(this,ctx);this.addField_MFString(ctx,'url',["ws://localhost:35668/cstreams/0"]);this.addField_MFString(ctx,'label',[]);this.addField_SFInt32(ctx,'maxRenderedIds',-1);this.addField_SFBool(ctx,'reconnect',true);this.addField_SFFloat(ctx,'scaleRenderedIdsOnMove',1.0);this.addField_SFBool(ctx,'enableCulling',true);this.addField_MFString(ctx,'invisibleNodes',[]);this.addField_SFBool(ctx,'internalCulling',false);this._idList=[];this._websocket=null;this._nameObjMap={};this._createTime=[];this._visibleList=[];if(ctx)
this.initializeSocket();else
x3dom.debug.logWarning("RemoteSelectionGroup: No runtime context found!");},{initializeSocket:function()
{var that=this;if("WebSocket"in window)
{var wsUrl="ws://localhost:35668/cstreams/0";if(this._vf.url.length&&this._vf.url[0].length)
wsUrl=this._vf.url[0];this._websocket=new WebSocket(wsUrl);this._websocket._lastMsg=null;this._websocket._lastData="";this._websocket.onopen=function(evt)
{x3dom.debug.logInfo("WS Connected");var view=that._nameSpace.doc._viewarea.getViewMatrix();this._lastMsg=view.toGL().toString();view=that._nameSpace.doc._viewarea.getProjectionMatrix();this._lastMsg+=(","+view.toGL().toString());this.send(this._lastMsg);x3dom.debug.logInfo("WS Sent: "+this._lastMsg);this._lastMsg="";this._lastData="";};this._websocket.onclose=function(evt)
{x3dom.debug.logInfo("WS Disconnected");if(that._vf.reconnect)
{window.setTimeout(function(){that.initializeSocket();},2000);}};this._websocket.onmessage=function(evt)
{if(that._vf.maxRenderedIds<0)
{that._idList=x3dom.fields.MFString.parse(evt.data);}
else if(that._vf.maxRenderedIds>0)
{that._idList=[];var arr=x3dom.fields.MFString.parse(evt.data);var n=Math.min(arr.length,Math.abs(that._vf.maxRenderedIds));for(var i=0;i<n;++i){that._idList[i]=arr[i];}}
if(that._vf.maxRenderedIds!=0&&this._lastData!=evt.data)
{this._lastData=evt.data;that._nameSpace.doc.needRender=true;}};this._websocket.onerror=function(evt)
{x3dom.debug.logError(evt.data);};this._websocket.updateCamera=function()
{var view=that._nameSpace.doc._viewarea.getViewMatrix();var message=view.toGL().toString();view=that._nameSpace.doc._viewarea.getProjectionMatrix();message+=(","+view.toGL().toString());if(this._lastMsg!=null&&this._lastMsg!=message)
{this._lastMsg=message;this.send(message);}};}
else
{x3dom.debug.logError("Browser has no WebSocket support!");}},nodeChanged:function()
{var n=this._vf.label.length;this._nameObjMap={};this._createTime=new Array(n);this._visibleList=new Array(n);for(var i=0;i<n;++i)
{var shape=this._childNodes[i];if(shape&&x3dom.isa(shape,x3dom.nodeTypes.X3DShapeNode))
{this._nameObjMap[this._vf.label[i]]={shape:shape,pos:i};this._visibleList[i]=true;}
else{this._visibleList[i]=false;x3dom.debug.logError("Invalid children: "+this._vf.label[i]);}
this._createTime[i]=0;}
x3dom.debug.logInfo("RemoteSelectionGroup has "+n+" entries.");},fieldChanged:function(fieldName)
{if(fieldName=="url")
{if(this._websocket){this._websocket.close();this._websocket=null;}
this.initializeSocket();}
else if(fieldName=="invisibleNodes")
{for(var i=0,n=this._vf.label.length;i<n;++i)
{var shape=this._childNodes[i];if(shape&&x3dom.isa(shape,x3dom.nodeTypes.X3DShapeNode))
{this._visibleList[i]=true;for(var j=0,numInvis=this._vf.invisibleNodes.length;j<numInvis;++j)
{var nodeName=this._vf.invisibleNodes[j];var starInd=nodeName.lastIndexOf('*');var matchNameBegin=false;if(starInd>0){nodeName=nodeName.substring(0,starInd);matchNameBegin=true;}
if(nodeName.length<=1)
continue;if((matchNameBegin&&this._vf.label[i].indexOf(nodeName)==0)||this._vf.label[i]==nodeName){this._visibleList[i]=false;break;}}}
else{this._visibleList[i]=false;}}}},getNumRenderedObjects:function(len,isMoving)
{var n=len;if(this._vf.maxRenderedIds>0)
{var num=Math.max(this._vf.maxRenderedIds,16);var scale=1;if(isMoving)
scale=Math.min(this._vf.scaleRenderedIdsOnMove,1);num=Math.max(Math.round(scale*num),0);n=Math.min(n,num);}
return n;},collectDrawableObjects:function(transform,out)
{if(!this._vf.render||!out){return;}
out.cnt++;var viewarea=this._nameSpace.doc._viewarea;var isMoving=viewarea.isMoving();var ts=new Date().getTime();var maxLiveTime=10000;var i,n,numChild=this._childNodes.length;if(!this._vf.enableCulling)
{n=this.getNumRenderedObjects(numChild,isMoving);var view_frustum=null,box=null;if(this._vf.internalCulling==true)
{var viewpoint=viewarea._scene.getViewpoint();var near=viewpoint.getNear();var proj=viewarea.getProjectionMatrix();var view=viewarea.getViewMatrix();view_frustum=new x3dom.fields.FrustumVolume(proj.mult(view));var trafo=this.getCurrentTransform();var modelView=view.mult(trafo);var imgPlaneHeightAtDistOne=viewpoint.getImgPlaneHeightAtDistOne();imgPlaneHeightAtDistOne/=this._nameSpace.doc._viewarea._height;box=new x3dom.fields.BoxVolume();}
for(i=0,cnt=0;i<numChild;i++)
{var shape=this._childNodes[i];if(shape)
{var needCleanup=true;if(this._visibleList[i]&&cnt<n)
{if(view_frustum)
{shape.getVolume(box.min,box.max);box.transform(trafo);}
if(!view_frustum||view_frustum.intersect(box))
{var pxThreshold=20,numPixel=pxThreshold;if(view_frustum)
{var center=modelView.multMatrixPnt(shape.getCenter());var dia=shape.getDiameter();var dist=Math.max(-center.z-dia/2,near);var projPixelLength=dist*imgPlaneHeightAtDistOne;numPixel=dia/projPixelLength;}
if(numPixel>=pxThreshold)
{shape.collectDrawableObjects(transform,out);this._createTime[i]=ts;cnt++;needCleanup=false;}}}
if(needCleanup&&!isMoving&&this._createTime[i]>0&&ts-this._createTime[i]>maxLiveTime&&shape._cleanupGLObjects)
{shape._cleanupGLObjects(true);this._createTime[i]=0;}}}
return;}
if(this._websocket)
this._websocket.updateCamera();if(this._vf.label.length)
{n=this.getNumRenderedObjects(this._idList.length,isMoving);for(i=0;i<n;i++)
{var obj=this._nameObjMap[this._idList[i]];if(obj&&obj.shape){obj.shape.collectDrawableObjects(transform,out);this._createTime[obj.pos]=ts;}
else
x3dom.debug.logError("Invalid label: "+this._idList[i]);}
for(i=0;i<this._childNodes.length;i++)
{if(this._childNodes[i]&&!isMoving&&this._createTime[i]>0&&ts-this._createTime[i]>maxLiveTime&&this._childNodes[i]._cleanupGLObjects)
{this._childNodes[i]._cleanupGLObjects(true);this._createTime[i]=0;}}}}}));x3dom.registerNodeType("Scene","Core",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Scene.superClass.call(this,ctx);this.addField_SFString(ctx,'pickMode',"idBuf");this.addField_SFBool(ctx,'doPickPass',true);this.addField_SFString(ctx,'shadowObjectIdMapping',"");this.addField_SFBool(ctx,'isStaticHierarchy',false);this.addField_SFBool(ctx,'sortTrans',true);this.addField_SFBool(ctx,'frustumCulling',true);this._lastMin=null;this._lastMax=null;this._shadowIdMap=null;this.drawableObjects=null;},{nodeChanged:function()
{this.loadMapping();},fieldChanged:function(fieldName)
{if(fieldName=="shadowObjectIdMapping")
this.loadMapping();},updateVolume:function()
{var min=x3dom.fields.SFVec3f.MAX();var max=x3dom.fields.SFVec3f.MIN();if(this.getVolume(min,max)){this._lastMin=min;this._lastMax=max;}},loadMapping:function()
{this._shadowIdMap=null;if(this._vf.shadowObjectIdMapping.length==0){return;}
var that=this;var xhr=new XMLHttpRequest();xhr.open("GET",encodeURI(this._nameSpace.getURL(this._vf.shadowObjectIdMapping)),true);xhr.send();xhr.onload=function()
{that._shadowIdMap=eval("("+xhr.response+")");};}}));x3dom.BindableStack=function(doc,type,defaultType,getter){this._doc=doc;this._type=type;this._defaultType=defaultType;this._defaultRoot=null;this._getter=getter;this._bindBag=[];this._bindStack=[];};x3dom.BindableStack.prototype.top=function(){return((this._bindStack.length>0)?this._bindStack[this._bindStack.length-1]:null);};x3dom.BindableStack.prototype.push=function(bindable){var top=this.top();if(top===bindable){return;}
if(top){top.deactivate();}
this._bindStack.push(bindable);bindable.activate(top);};x3dom.BindableStack.prototype.replaceTop=function(bindable){var top=this.top();if(top===bindable){return;}
if(top){top.deactivate();this._bindStack[this._bindStack.length-1]=bindable;bindable.activate(top);}};x3dom.BindableStack.prototype.pop=function(bindable){var top;if(bindable){top=this.top();if(bindable!==top){return null;}}
top=this._bindStack.pop();if(top){top.deactivate();}
return top;};x3dom.BindableStack.prototype.switchTo=function(target){var last=this.getActive();var n=this._bindBag.length;var toBind=0;var i=0,lastIndex=-1;if(n<=1){return;}
switch(target)
{case'first':toBind=this._bindBag[0];break;case'last':toBind=this._bindBag[n-1];break;default:for(i=0;i<n;i++){if(this._bindBag[i]==last){lastIndex=i;break;}}
if(lastIndex>=0){i=lastIndex;while(!toBind){if(target=='next'){i=(i<(n-1))?(i+1):0;}else{i=(i>0)?(i-1):(n-1);}
if(i==lastIndex){break;}
if(this._bindBag[i]._vf.description.length>=0){toBind=this._bindBag[i];}}}
break;}
if(toBind){this.replaceTop(toBind);}else{x3dom.debug.logWarning('Cannot switch bindable; no other bindable with description found.');}};x3dom.BindableStack.prototype.getActive=function(){if(this._bindStack.length===0){if(this._bindBag.length===0){if(this._defaultRoot){x3dom.debug.logInfo('create new '+this._defaultType._typeName+' for '+this._type._typeName+'-stack');var obj=new this._defaultType({doc:this._doc,nameSpace:this._defaultRoot._nameSpace,autoGen:true});this._defaultRoot.addChild(obj);obj.nodeChanged();}
else{x3dom.debug.logError('stack without defaultRoot');}}
else{x3dom.debug.logInfo('activate first '+this._type._typeName+' for '+this._type._typeName+'-stack');}
this._bindStack.push(this._bindBag[0]);this._bindBag[0].activate();}
return this._bindStack[this._bindStack.length-1];};x3dom.BindableBag=function(doc){this._stacks=[];this.addType("X3DViewpointNode","Viewpoint","getViewpoint",doc);this.addType("X3DNavigationInfoNode","NavigationInfo","getNavigationInfo",doc);this.addType("X3DBackgroundNode","Background","getBackground",doc);this.addType("X3DFogNode","Fog","getFog",doc);};x3dom.BindableBag.prototype.addType=function(typeName,defaultTypeName,getter,doc){var type=x3dom.nodeTypes[typeName];var defaultType=x3dom.nodeTypes[defaultTypeName];if(type&&defaultType){var stack=new x3dom.BindableStack(doc,type,defaultType,getter);this._stacks.push(stack);}
else{x3dom.debug.logWarning('Invalid Bindable type/defaultType: '+
typeName+'/'+defaultType);}};x3dom.BindableBag.prototype.setRefNode=function(node){Array.forEach(this._stacks,function(stack){stack._defaultRoot=node;node[stack._getter]=function(){return stack.getActive();};});};x3dom.BindableBag.prototype.addBindable=function(node){for(var i=0,n=this._stacks.length;i<n;i++){var stack=this._stacks[i];if(x3dom.isa(node,stack._type)){x3dom.debug.logInfo('register '+node.typeName()+'Bindable '+
node._DEF+'/'+node._vf.description);stack._bindBag.push(node);var top=stack.top();if(top&&top._autoGen){stack.replaceTop(node);for(var j=0,m=stack._bindBag.length;j<m;j++){if(stack._bindBag[j]===top){stack._bindBag.splice(j,1);break;}}
stack._defaultRoot.removeChild(top);}
return stack;}}
x3dom.debug.logError(node.typeName()+' is not a valid bindable');return null;};x3dom.registerNodeType("X3DGeometryNode","Rendering",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DGeometryNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'solid',true);this.addField_SFBool(ctx,'ccw',true);this.addField_SFBool(ctx,'useGeoCache',true);this._mesh=new x3dom.Mesh(this);this._pickable=true;},{getVolume:function(min,max){return this._mesh.getVolume(min,max);},invalidateVolume:function(){this._mesh.invalidate();},getCenter:function(){return this._mesh.getCenter();},getDiameter:function(){return this._mesh.getDiameter();},doIntersect:function(line){if(this._pickable){return this._mesh.doIntersect(line);}
else{return false;}},getColorTexture:function(){return null;},getColorTextureURL:function(){return null;},parentAdded:function(parent){if(parent._cleanupGLObjects){parent._cleanupGLObjects(true);}
parent.setAllDirty();}}));x3dom.registerNodeType("Mesh","Rendering",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.Mesh.superClass.call(this,ctx);this.addField_SFString(ctx,'primType',"triangle");this.addField_MFInt32(ctx,'index',[]);this.addField_MFNode('vertexAttributes',x3dom.nodeTypes.X3DVertexAttributeNode);},{nodeChanged:function()
{var time0=new Date().getTime();var i,n=this._cf.vertexAttributes.nodes.length;for(i=0;i<n;i++)
{var name=this._cf.vertexAttributes.nodes[i]._vf.name;switch(name.toLowerCase())
{case"position":this._mesh._positions[0]=this._cf.vertexAttributes.nodes[i]._vf.value.toGL();break;case"normal":this._mesh._normals[0]=this._cf.vertexAttributes.nodes[i]._vf.value.toGL();break;case"texcoord":this._mesh._texCoords[0]=this._cf.vertexAttributes.nodes[i]._vf.value.toGL();break;case"color":this._mesh._colors[0]=this._cf.vertexAttributes.nodes[i]._vf.value.toGL();break;default:this._mesh._dynamicFields[name]={};this._mesh._dynamicFields[name].numComponents=this._cf.vertexAttributes.nodes[i]._vf.numComponents;this._mesh._dynamicFields[name].value=this._cf.vertexAttributes.nodes[i]._vf.value.toGL();break;}}
this._mesh._indices[0]=this._vf.index.toGL();this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;var time1=new Date().getTime()-time0;x3dom.debug.logWarning("Mesh load time: "+time1+" ms");}}));x3dom.registerNodeType("PointSet","Rendering",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.PointSet.superClass.call(this,ctx);this.addField_SFNode('coord',x3dom.nodeTypes.Coordinate);this.addField_SFNode('color',x3dom.nodeTypes.X3DColorNode);this._pickable=false;},{nodeChanged:function()
{var time0=new Date().getTime();var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);var positions=coordNode._vf.point;var numColComponents=3;var colorNode=this._cf.color.node;var colors=new x3dom.fields.MFColor();if(colorNode){colors=colorNode._vf.color;x3dom.debug.assert(positions.length==colors.length);if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
this._mesh._numColComponents=numColComponents;this._mesh._indices[0]=[];this._mesh._positions[0]=positions.toGL();this._mesh._colors[0]=colors.toGL();this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._lit=false;this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;var time1=new Date().getTime()-time0;},fieldChanged:function(fieldName)
{var pnts=null;if(fieldName=="coord")
{pnts=this._cf.coord.node._vf.point;this._mesh._positions[0]=pnts.toGL();this._mesh._invalidate=true;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}
else if(fieldName=="color")
{pnts=this._cf.color.node._vf.color;this._mesh._colors[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.colors=true;});}}}));x3dom.registerNodeType("X3DComposedGeometryNode","Rendering",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.X3DComposedGeometryNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'colorPerVertex',true);this.addField_SFBool(ctx,'normalPerVertex',true);this.addField_MFNode('attrib',x3dom.nodeTypes.X3DVertexAttributeNode);this.addField_SFNode('coord',x3dom.nodeTypes.X3DCoordinateNode);this.addField_SFNode('normal',x3dom.nodeTypes.Normal);this.addField_SFNode('color',x3dom.nodeTypes.X3DColorNode);this.addField_SFNode('texCoord',x3dom.nodeTypes.X3DTextureCoordinateNode);},{handleAttribs:function()
{var i,n=this._cf.attrib.nodes.length;for(i=0;i<n;i++)
{var name=this._cf.attrib.nodes[i]._vf.name;switch(name.toLowerCase())
{case"position":this._mesh._positions[0]=this._cf.attrib.nodes[i]._vf.value.toGL();break;case"normal":this._mesh._normals[0]=this._cf.attrib.nodes[i]._vf.value.toGL();break;case"texcoord":this._mesh._texCoords[0]=this._cf.attrib.nodes[i]._vf.value.toGL();break;case"color":this._mesh._colors[0]=this._cf.attrib.nodes[i]._vf.value.toGL();break;default:this._mesh._dynamicFields[name]={};this._mesh._dynamicFields[name].numComponents=this._cf.attrib.nodes[i]._vf.numComponents;this._mesh._dynamicFields[name].value=this._cf.attrib.nodes[i]._vf.value.toGL();break;}}}}));x3dom.registerNodeType("IndexedLineSet","Rendering",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.IndexedLineSet.superClass.call(this,ctx);this.addField_SFBool(ctx,'colorPerVertex',true);this.addField_MFNode('attrib',x3dom.nodeTypes.X3DVertexAttributeNode);this.addField_SFNode('coord',x3dom.nodeTypes.X3DCoordinateNode);this.addField_SFNode('color',x3dom.nodeTypes.X3DColorNode);this.addField_MFInt32(ctx,'coordIndex',[]);this.addField_MFInt32(ctx,'colorIndex',[]);this._pickable=false;},{nodeChanged:function()
{var time0=new Date().getTime();var indexes=this._vf.coordIndex;var colorInd=this._vf.colorIndex;var hasColor=false,hasColorInd=false;var colPerVert=this._vf.colorPerVertex;if(colorInd.length>0)
{hasColorInd=true;}
var positions,colors;var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);positions=coordNode.getPoints();var numColComponents=3;var colorNode=this._cf.color.node;if(colorNode)
{hasColor=true;colors=colorNode._vf.color;if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
else{hasColor=false;}
this._mesh._indices[0]=[];this._mesh._positions[0]=[];this._mesh._colors[0]=[];var i,t,cnt,lineCnt;var p0,p1,c0,c1;if((hasColor&&hasColorInd))
{t=0;cnt=0;lineCnt=0;for(i=0;i<indexes.length;++i)
{if(indexes[i]===-1){t=0;continue;}
if(hasColorInd){x3dom.debug.assert(colorInd[i]!=-1);}
switch(t)
{case 0:p0=+indexes[i];if(hasColorInd&&colPerVert){c0=+colorInd[i];}
else{c0=p0;}
t=1;break;case 1:p1=+indexes[i];if(hasColorInd&&colPerVert){c1=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c1=+colorInd[lineCnt];}
else{c1=p1;}
this._mesh._indices[0].push(cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);if(hasColor){if(!colPerVert){c0=c1;}
this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);}
t=2;lineCnt++;break;case 3:p0=p1;c0=c1;p1=+indexes[i];if(hasColorInd&&colPerVert){c1=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c1=+colorInd[lineCnt];}
else{c1=p1;}
this._mesh._indices[0].push(cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);if(hasColor){if(!colPerVert){c0=c1;}
this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);}
lineCnt++;break;default:}}}
else
{t=0;for(i=0;i<indexes.length;++i)
{if(indexes[i]===-1){t=0;continue;}
switch(t){case 0:p0=+indexes[i];t=1;break;case 1:p1=+indexes[i];t=2;this._mesh._indices[0].push(p0,p1);break;case 2:p0=p1;p1=+indexes[i];this._mesh._indices[0].push(p0,p1);break;}}
this._mesh._positions[0]=positions.toGL();if(hasColor){this._mesh._colors[0]=colors.toGL();this._mesh._numColComponents=numColComponents;}}
this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;var time1=new Date().getTime()-time0;},fieldChanged:function(fieldName)
{var pnts=null;if(fieldName=="coord")
{pnts=this._cf.coord.node._vf.point;this._mesh._positions[0]=pnts.toGL();this._mesh._invalidate=true;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}
else if(fieldName=="color")
{pnts=this._cf.color.node._vf.color;this._mesh._colors[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.colors=true;});}}}));x3dom.registerNodeType("IndexedTriangleSet","Rendering",defineClass(x3dom.nodeTypes.X3DComposedGeometryNode,function(ctx){x3dom.nodeTypes.IndexedTriangleSet.superClass.call(this,ctx);this.addField_MFInt32(ctx,'index',[]);},{nodeChanged:function()
{var time0=new Date().getTime();this.handleAttribs();var colPerVert=this._vf.colorPerVertex;var normPerVert=this._vf.normalPerVertex;var indexes=this._vf.index;var hasNormal=false,hasTexCoord=false,hasColor=false;var positions,normals,texCoords,colors;var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);positions=coordNode._vf.point;var normalNode=this._cf.normal.node;if(normalNode){hasNormal=true;normals=normalNode._vf.vector;}
else{hasNormal=false;}
var texMode="",numTexComponents=2;var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if(texCoordNode){if(texCoordNode._vf.point){hasTexCoord=true;texCoords=texCoordNode._vf.point;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}}
else if(texCoordNode._vf.mode){texMode=texCoordNode._vf.mode;}}
else{hasTexCoord=false;}
var numColComponents=3;var colorNode=this._cf.color.node;if(colorNode){hasColor=true;colors=colorNode._vf.color;if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
else{hasColor=false;}
this._mesh._indices[0]=[];this._mesh._positions[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._colors[0]=[];var i,t,cnt,faceCnt,posMax;var p0,p1,p2,n0,n1,n2,t0,t1,t2,c0,c1,c2;while(positions.length%3>0){positions.push(positions.length-1);}
posMax=positions.length;if(!normPerVert||positions.length>65535)
{t=0;cnt=0;faceCnt=0;this._mesh._multiIndIndices=[];this._mesh._posSize=positions.length;for(i=0;i<indexes.length;++i)
{if((i>0)&&(i%3===0)){t=0;faceCnt++;}
switch(t)
{case 0:p0=+indexes[i];if(normPerVert){n0=p0;}else if(!normPerVert){n0=faceCnt;}
t0=p0;if(colPerVert){c0=p0;}else if(!colPerVert){c0=faceCnt;}
t=1;break;case 1:p1=+indexes[i];if(normPerVert){n1=p1;}else if(!normPerVert){n1=faceCnt;}
t1=p1;if(colPerVert){c1=p1;}else if(!colPerVert){c1=faceCnt;}
t=2;break;case 2:p2=+indexes[i];if(normPerVert){n2=p2;}else if(!normPerVert){n2=faceCnt;}
t2=p2;if(colPerVert){c2=p2;}else if(!colPerVert){c2=faceCnt;}
t=3;this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);if(hasNormal){this._mesh._normals[0].push(normals[n0].x);this._mesh._normals[0].push(normals[n0].y);this._mesh._normals[0].push(normals[n0].z);this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);}
else{this._mesh._multiIndIndices.push(p0,p1,p2);}
if(hasColor){this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c0].a);}
this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t0].x);this._mesh._texCoords[0].push(texCoords[t0].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t0].z);}
this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}}
break;default:}}
if(!hasNormal){this._mesh.calcNormals(normPerVert?Math.PI:0);}
if(!hasTexCoord){this._mesh.calcTexCoords(texMode);}
this._mesh.splitMesh();}
else
{faceCnt=0;for(i=0;i<indexes.length;i++)
{if((i>0)&&(i%3===0)){faceCnt++;}
this._mesh._indices[0].push(indexes[i]);if(!normPerVert&&hasNormal){this._mesh._normals[0].push(normals[faceCnt].x);this._mesh._normals[0].push(normals[faceCnt].y);this._mesh._normals[0].push(normals[faceCnt].z);}
if(!colPerVert&&hasColor){this._mesh._colors[0].push(colors[faceCnt].r);this._mesh._colors[0].push(colors[faceCnt].g);this._mesh._colors[0].push(colors[faceCnt].b);if(numColComponents===4){this._mesh._colors[0].push(colors[faceCnt].a);}}}
this._mesh._positions[0]=positions.toGL();if(hasNormal){this._mesh._normals[0]=normals.toGL();}
else{this._mesh.calcNormals(normPerVert?Math.PI:0);}
if(hasTexCoord){this._mesh._texCoords[0]=texCoords.toGL();this._mesh._numTexComponents=numTexComponents;}
else{this._mesh.calcTexCoords(texMode);}
if(hasColor&&colPerVert){this._mesh._colors[0]=colors.toGL();this._mesh._numColComponents=numColComponents;}}
this._mesh._invalidate=true;this._mesh._numFaces=0;this._mesh._numCoords=0;for(i=0;i<this._mesh._indices.length;i++){this._mesh._numFaces+=this._mesh._indices[i].length/3;this._mesh._numCoords+=this._mesh._positions[i].length/3;}
var time1=new Date().getTime()-time0;},fieldChanged:function(fieldName)
{var pnts=this._cf.coord.node._vf.point;if(pnts.length>65535)
{x3dom.debug.logWarning("IndexedTriangleSet: fieldChanged with "+"too many coordinates not yet implemented!");return;}
if(fieldName=="coord")
{this._mesh._positions[0]=pnts.toGL();this._mesh._invalidate=true;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}
else if(fieldName=="color")
{pnts=this._cf.color.node._vf.color;if(this._vf.colorPerVertex){this._mesh._colors[0]=pnts.toGL();}else if(!this._vf.colorPerVertex){var faceCnt=0;var numColComponents=3;if(x3dom.isa(this._cf.color.node,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}
this._mesh._colors[0]=[];var indexes=this._vf.index;for(i=0;i<indexes.length;++i)
{if((i>0)&&(i%3===0)){faceCnt++;}
this._mesh._colors[0].push(pnts[faceCnt].r);this._mesh._colors[0].push(pnts[faceCnt].g);this._mesh._colors[0].push(pnts[faceCnt].b);if(numColComponents===4){this._mesh._colors[0].push(pnts[faceCnt].a);}}}
Array.forEach(this._parentNodes,function(node){node._dirty.colors=true;});}
else if(fieldName=="normal")
{pnts=this._cf.normal.node._vf.vector;if(this._vf.normalPerVertex){this._mesh._normals[0]=pnts.toGL();}else if(!this._vf.normalPerVertex){var indexes=this._vf.index;this._mesh._normals[0]=[];var faceCnt=0;for(i=0;i<indexes.length;++i)
{if((i>0)&&(i%3===0)){faceCnt++;}
this._mesh._normals[0].push(pnts[faceCnt].x);this._mesh._normals[0].push(pnts[faceCnt].y);this._mesh._normals[0].push(pnts[faceCnt].z);}}
Array.forEach(this._parentNodes,function(node){node._dirty.normals=true;});}
else if(fieldName=="texCoord")
{var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
pnts=texCoordNode._vf.point;this._mesh._texCoords[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.texcoords=true;});}}}));x3dom.registerNodeType("IndexedTriangleStripSet","Rendering",defineClass(x3dom.nodeTypes.X3DComposedGeometryNode,function(ctx){x3dom.nodeTypes.IndexedTriangleStripSet.superClass.call(this,ctx);this.addField_MFInt32(ctx,'index',[]);},{nodeChanged:function()
{this.handleAttribs();var hasNormal=false,hasTexCoord=false,hasColor=false;var colPerVert=this._vf.colorPerVertex;var normPerVert=this._vf.normalPerVertex;var indexes=this._vf.index;var positions,normals,texCoords,colors;var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);positions=coordNode._vf.point;var normalNode=this._cf.normal.node;if(normalNode){hasNormal=true;normals=normalNode._vf.vector;}
else{hasNormal=false;}
var texMode="",numTexComponents=2;var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if(texCoordNode){if(texCoordNode._vf.point){hasTexCoord=true;texCoords=texCoordNode._vf.point;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}}
else if(texCoordNode._vf.mode){texMode=texCoordNode._vf.mode;}}
else{hasTexCoord=false;}
this._mesh._numTexComponents=numTexComponents;var numColComponents=3;var colorNode=this._cf.color.node;if(colorNode){hasColor=true;colors=colorNode._vf.color;if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
else{hasColor=false;}
this._mesh._numColComponents=numColComponents;this._mesh._indices[0]=[];this._mesh._positions[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._colors[0]=[];this._mesh._invalidate=true;this._mesh._numFaces=0;this._mesh._numCoords=0;var faceCnt=0,cnt=0;if(hasNormal&&positions.length<=65535)
{this._mesh._primType='TRIANGLESTRIP';this._indexOffset=[];this._indexOffset.push(0);for(i=0;i<indexes.length;i++)
{if(indexes[i]==-1){faceCnt++;this._indexOffset.push(this._mesh._indices[0].length);continue;}
else{this._mesh._indices[0].push(+indexes[i]);if(!normPerVert){this._mesh._normals[0].push(normals[faceCnt].x);this._mesh._normals[0].push(normals[faceCnt].y);this._mesh._normals[0].push(normals[faceCnt].z);}
if(!colPerVert){this._mesh._colors[0].push(colors[faceCnt].r);this._mesh._colors[0].push(colors[faceCnt].g);this._mesh._colors[0].push(colors[faceCnt].b);if(numColComponents===4){this._mesh._colors[0].push(colors[faceCnt].a);}}}}
this._mesh._positions[0]=positions.toGL();if(normPerVert){this._mesh._normals[0]=normals.toGL();}
if(hasTexCoord){this._mesh._texCoords[0]=texCoords.toGL();this._mesh._numTexComponents=numTexComponents;}
else{x3dom.debug.logWarning("IndexedTriangleStripSet: no texCoords given and won't calculate!");}
if(hasColor){if(colPerVert){this._mesh._colors[0]=colors.toGL();}
this._mesh._numColComponents=numColComponents;}
for(i=1;i<this._indexOffset.length;i++){this._mesh._numFaces+=(this._indexOffset[i]-this._indexOffset[i-1]-2);}
this._mesh._numCoords=this._mesh._positions[0].length/3;}
else
{var p1,p2,p3,n1,n2,n3,t1,t2,t3,c1,c2,c3;var swapOrder=false;for(i=1;i<indexes.length-2;++i)
{if(indexes[i+1]==-1){i=i+2;faceCnt++;continue;}
if(swapOrder){p1=indexes[i];p2=indexes[i-1];p3=indexes[i+1];}
else{p1=indexes[i-1];p2=indexes[i];p3=indexes[i+1];}
swapOrder=!swapOrder;if(normPerVert){n1=p1;n2=p2;n3=p3;}else if(!normPerVert){n1=n2=n3=faceCnt;}
t1=p1;t2=p2;t3=p3;if(colPerVert){c1=p1;c2=p2;c3=p3;}else if(!colPerVert){c1=c2=c3=faceCnt;}
this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);this._mesh._positions[0].push(positions[p3].x);this._mesh._positions[0].push(positions[p3].y);this._mesh._positions[0].push(positions[p3].z);if(hasNormal){this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);this._mesh._normals[0].push(normals[n3].x);this._mesh._normals[0].push(normals[n3].y);this._mesh._normals[0].push(normals[n3].z);}
if(hasColor){this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}
this._mesh._colors[0].push(colors[c3].r);this._mesh._colors[0].push(colors[c3].g);this._mesh._colors[0].push(colors[c3].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c3].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}
this._mesh._texCoords[0].push(texCoords[t3].x);this._mesh._texCoords[0].push(texCoords[t3].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t3].z);}}}
if(!hasNormal){this._mesh.calcNormals(Math.PI);}
if(!hasTexCoord){this._mesh.calcTexCoords(texMode);}
this._mesh.splitMesh();for(i=0;i<this._mesh._indices.length;i++){this._mesh._numFaces+=this._mesh._indices[i].length/3;this._mesh._numCoords+=this._mesh._positions[i].length/3;}}},fieldChanged:function(fieldName)
{if(fieldName!="coord"&&fieldName!="normal"&&fieldName!="texCoord"&&fieldName!="color")
{x3dom.debug.logWarning("IndexedTriangleStripSet: fieldChanged for "+
fieldName+" not yet implemented!");return;}
var pnts=this._cf.coord.node._vf.point;if((this._cf.normal.node===null)||(pnts.length>65535))
{if(fieldName=="coord"){this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];var hasNormal=false,hasTexCoord=false,hasColor=false;var colPerVert=this._vf.colorPerVertex;var normPerVert=this._vf.normalPerVertex;var indexes=this._vf.index;var positions,normals,texCoords,colors;var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);positions=coordNode._vf.point;var normalNode=this._cf.normal.node;if(normalNode){hasNormal=true;normals=normalNode._vf.vector;}
else{hasNormal=false;}
var texMode="",numTexComponents=2;var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if(texCoordNode){if(texCoordNode._vf.point){hasTexCoord=true;texCoords=texCoordNode._vf.point;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}}
else if(texCoordNode._vf.mode){texMode=texCoordNode._vf.mode;}}
else{hasTexCoord=false;}
this._mesh._numTexComponents=numTexComponents;var numColComponents=3;var colorNode=this._cf.color.node;if(colorNode){hasColor=true;colors=colorNode._vf.color;if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
else{hasColor=false;}
this._mesh._numColComponents=numColComponents;this._mesh._indices[0]=[];this._mesh._positions[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._colors[0]=[];var faceCnt=0,cnt=0;var p1,p2,p3,n1,n2,n3,t1,t2,t3,c1,c2,c3;var swapOrder=false;if(hasNormal||hasTexCoord||hasColor){for(i=1;i<indexes.length-2;++i)
{if(indexes[i+1]==-1){i=i+2;faceCnt++;continue;}
if(swapOrder){p1=indexes[i];p2=indexes[i-1];p3=indexes[i+1];}
else{p1=indexes[i-1];p2=indexes[i];p3=indexes[i+1];}
swapOrder=!swapOrder;if(normPerVert){n1=p1;n2=p2;n3=p3;}else if(!normPerVert){n1=n2=n3=faceCnt;}
t1=p1;t2=p2;t3=p3;if(colPerVert){c1=p1;c2=p2;c3=p3;}else if(!colPerVert){c1=c2=c3=faceCnt;}
this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);this._mesh._positions[0].push(positions[p3].x);this._mesh._positions[0].push(positions[p3].y);this._mesh._positions[0].push(positions[p3].z);if(hasNormal){this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);this._mesh._normals[0].push(normals[n3].x);this._mesh._normals[0].push(normals[n3].y);this._mesh._normals[0].push(normals[n3].z);}
if(hasColor){this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}
this._mesh._colors[0].push(colors[c3].r);this._mesh._colors[0].push(colors[c3].g);this._mesh._colors[0].push(colors[c3].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c3].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}
this._mesh._texCoords[0].push(texCoords[t3].x);this._mesh._texCoords[0].push(texCoords[t3].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t3].z);}}}
if(!hasNormal){this._mesh.calcNormals(Math.PI);}
if(!hasTexCoord){this._mesh.calcTexCoords(texMode);}
this._mesh.splitMesh();}else{var swapOrder=false;for(i=1;i<indexes.length;++i)
{if(indexes[i+1]==-1){i=i+2;continue;}
if(swapOrder){this._mesh._indices[0].push(indexes[i])
this._mesh._indices[0].push(indexes[i-1])
this._mesh._indices[0].push(indexes[i+1])}
else{this._mesh._indices[0].push(indexes[i-1])
this._mesh._indices[0].push(indexes[i])
this._mesh._indices[0].push(indexes[i+1])}
swapOrder=!swapOrder;}
this._mesh._positions[0]=positions.toGL();if(hasNormal){this._mesh._normals[0]=normals.toGL();}
else{this._mesh.calcNormals(Math.PI);}
if(hasTexCoord){this._mesh._texCoords[0]=texCoords.toGL();this._mesh._numTexComponents=numTexComponents;}
else{this._mesh.calcTexCoords(texMode);}
if(hasColor){this._mesh._colors[0]=colors.toGL();this._mesh._numColComponents=numColComponents;}}
this._mesh._invalidate=true;this._mesh._numFaces=0;this._mesh._numCoords=0;for(i=0;i<this._mesh._indices.length;i++){this._mesh._numFaces+=this._mesh._indices[i].length/3;this._mesh._numCoords+=this._mesh._positions[i].length/3;}
Array.forEach(this._parentNodes,function(node){node.setAllDirty();});}
else if(fieldName=="color"){var col=this._cf.color.node._vf.color;var faceCnt=0;var c1=c2=c3=0;var numColComponents=3;if(x3dom.isa(this._cf.color.node,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}
this._mesh._colors[0]=[];var indexes=this._vf.index;var swapOrder=false;for(i=1;i<indexes.length-2;++i)
{if(indexes[i+1]==-1){i=i+2;faceCnt++;continue;}
if(this._vf.colorPerVertex){if(swapOrder){c1=indexes[i];c2=indexes[i-1];c3=indexes[i+1];}
else{c1=indexes[i-1];c2=indexes[i];c3=indexes[i+1];}
swapOrder=!swapOrder;}else if(!this._vf.colorPerVertex){c1=c2=c3=faceCnt;}
this._mesh._colors[0].push(col[c1].r);this._mesh._colors[0].push(col[c1].g);this._mesh._colors[0].push(col[c1].b);if(numColComponents===4){this._mesh._colors[0].push(col[c1].a);}
this._mesh._colors[0].push(col[c2].r);this._mesh._colors[0].push(col[c2].g);this._mesh._colors[0].push(col[c2].b);if(numColComponents===4){this._mesh._colors[0].push(col[c2].a);}
this._mesh._colors[0].push(col[c3].r);this._mesh._colors[0].push(col[c3].g);this._mesh._colors[0].push(col[c3].b);if(numColComponents===4){this._mesh._colors[0].push(col[c3].a);}}
Array.forEach(this._parentNodes,function(node){node._dirty.colors=true;});}
else if(fieldName=="normal"){var nor=this._cf.normal.node._vf.vector;var faceCnt=0;var n1=n2=n3=0;this._mesh._normals[0]=[];var indexes=this._vf.index;var swapOrder=false;for(i=1;i<indexes.length-2;++i)
{if(indexes[i+1]==-1){i=i+2;faceCnt++;continue;}
if(this._vf.normalPerVertex){if(swapOrder){n1=indexes[i];n2=indexes[i-1];n3=indexes[i+1];}
else{n1=indexes[i-1];n2=indexes[i];n3=indexes[i+1];}
swapOrder=!swapOrder;}else if(!this._vf.normalPerVertex){n1=n2=n3=faceCnt;}
this._mesh._normals[0].push(nor[n1].x);this._mesh._normals[0].push(nor[n1].y);this._mesh._normals[0].push(nor[n1].z);this._mesh._normals[0].push(nor[n2].x);this._mesh._normals[0].push(nor[n2].y);this._mesh._normals[0].push(nor[n2].z);this._mesh._normals[0].push(nor[n3].x);this._mesh._normals[0].push(nor[n3].y);this._mesh._normals[0].push(nor[n3].z);}
Array.forEach(this._parentNodes,function(node){node._dirty.normals=true;});}
else if(fieldName=="texCoord"){var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
var tex=texCoordNode._vf.point;var t1=t2=t3=0;var numTexComponents=2;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}
this._mesh._texCoords[0]=[];var indexes=this._vf.index;var swapOrder=false;for(i=1;i<indexes.length-2;++i)
{if(indexes[i+1]==-1){i=i+2;continue;}
if(swapOrder){t1=indexes[i];t2=indexes[i-1];t3=indexes[i+1];}
else{t1=indexes[i-1];t2=indexes[i];t3=indexes[i+1];}
swapOrder=!swapOrder;this._mesh._texCoords[0].push(tex[t1].x);this._mesh._texCoords[0].push(tex[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(tex[t1].z);}
this._mesh._texCoords[0].push(tex[t2].x);this._mesh._texCoords[0].push(tex[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].tex(col[t2].z);}
this._mesh._texCoords[0].push(tex[t3].x);this._mesh._texCoords[0].push(tex[t3].y);if(numTexComponents===3){this._mesh._texCoords[0].push(tex[t3].z);}}
Array.forEach(this._parentNodes,function(node){node._dirty.texCoords=true;});}}
else
{if(fieldName=="coord")
{this._mesh._positions[0]=pnts.toGL();this._mesh._invalidate=true;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}
else if(fieldName=="color")
{pnts=this._cf.color.node._vf.color;if(this._vf.colorPerVertex){this._mesh._colors[0]=pnts.toGL();}else if(!this._vf.colorPerVertex){var faceCnt=0;var numColComponents=3;if(x3dom.isa(this._cf.color.node,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}
this._mesh._colors[0]=[];var indexes=this._vf.index;for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){faceCnt++;continue;}
this._mesh._colors[0].push(pnts[faceCnt].r);this._mesh._colors[0].push(pnts[faceCnt].g);this._mesh._colors[0].push(pnts[faceCnt].b);if(numColComponents===4){this._mesh._colors[0].push(pnts[faceCnt].a);}}}
x3dom.debug.logInfo();Array.forEach(this._parentNodes,function(node){node._dirty.colors=true;});}
else if(fieldName=="normal")
{pnts=this._cf.normal.node._vf.vector;if(this._vf.normalPerVertex){this._mesh._normals[0]=pnts.toGL();}else if(!this._vf.normalPerVertex){var indexes=this._vf.index;this._mesh._normals[0]=[];var faceCnt=0;for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){faceCnt++;continue;}
this._mesh._normals[0].push(pnts[faceCnt].x);this._mesh._normals[0].push(pnts[faceCnt].y);this._mesh._normals[0].push(pnts[faceCnt].z);}}
Array.forEach(this._parentNodes,function(node){node._dirty.normals=true;});}
else if(fieldName=="texCoord")
{var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
pnts=texCoordNode._vf.point;this._mesh._texCoords[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.texcoords=true;});}}}}));x3dom.registerNodeType("X3DGeometricPropertyNode","Rendering",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DGeometricPropertyNode.superClass.call(this,ctx);}));x3dom.registerNodeType("X3DCoordinateNode","Rendering",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.X3DCoordinateNode.superClass.call(this,ctx);},{fieldChanged:function(fieldName){if(fieldName==="coord"||fieldName==="point"){Array.forEach(this._parentNodes,function(node){node.fieldChanged("coord");});}},parentAdded:function(parent){if(parent._mesh&&parent._cf.coord.node!==this){parent.fieldChanged("coord");}}}));x3dom.registerNodeType("Coordinate","Rendering",defineClass(x3dom.nodeTypes.X3DCoordinateNode,function(ctx){x3dom.nodeTypes.Coordinate.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'point',[]);},{getPoints:function(){return this._vf.point;}}));x3dom.registerNodeType("Normal","Rendering",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.Normal.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'vector',[]);},{fieldChanged:function(fieldName){if(fieldName==="normal"||fieldName==="vector"){Array.forEach(this._parentNodes,function(node){node.fieldChanged("normal");});}},parentAdded:function(parent){if(parent._mesh&&parent._cf.normal.node!==this){parent.fieldChanged("normal");}}}));x3dom.registerNodeType("X3DColorNode","Rendering",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.X3DColorNode.superClass.call(this,ctx);},{fieldChanged:function(fieldName){if(fieldName==="color"){Array.forEach(this._parentNodes,function(node){node.fieldChanged("color");});}},parentAdded:function(parent){if(parent._mesh&&parent._cf.color.node!==this){parent.fieldChanged("color");}}}));x3dom.registerNodeType("Color","Rendering",defineClass(x3dom.nodeTypes.X3DColorNode,function(ctx){x3dom.nodeTypes.Color.superClass.call(this,ctx);this.addField_MFColor(ctx,'color',[]);}));x3dom.registerNodeType("ColorRGBA","Rendering",defineClass(x3dom.nodeTypes.X3DColorNode,function(ctx){x3dom.nodeTypes.ColorRGBA.superClass.call(this,ctx);this.addField_MFColorRGBA(ctx,'color',[]);}));x3dom.registerNodeType("X3DAppearanceNode","Shape",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DAppearanceNode.superClass.call(this,ctx);}));x3dom.registerNodeType("Appearance","Shape",defineClass(x3dom.nodeTypes.X3DAppearanceNode,function(ctx){x3dom.nodeTypes.Appearance.superClass.call(this,ctx);this.addField_SFNode('material',x3dom.nodeTypes.X3DMaterialNode);this.addField_SFNode('texture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('textureTransform',x3dom.nodeTypes.X3DTextureTransformNode);this.addField_SFNode('blendMode',x3dom.nodeTypes.BlendMode);this.addField_SFNode('depthMode',x3dom.nodeTypes.DepthMode);this.addField_MFNode('shaders',x3dom.nodeTypes.X3DShaderNode);this.addField_SFString(ctx,'sortType','auto');this.addField_SFInt32(ctx,'sortKey',0);this._shader=null;},{nodeChanged:function(){if(!this._cf.material.node){}
if(this._cf.shaders.nodes.length){this._shader=this._cf.shaders.nodes[0];}
if(this._vf.sortType=='auto'){if(this._cf.material.node&&this._cf.material.node._vf.transparency>0){this._vf.sortType='transparent';}
else if(this._cf.texture.node&&this._cf.texture.node._vf.url.length){if(this._cf.texture.node._vf.url[0].toLowerCase().indexOf('.'+'png')>=0){this._vf.sortType='transparent';}
else{this._vf.sortType='opaque';}}
else{this._vf.sortType='opaque';}}},texTransformMatrix:function(){if(this._cf.textureTransform.node===null){return x3dom.fields.SFMatrix4f.identity();}
else{return this._cf.textureTransform.node.texTransformMatrix();}},parentAdded:function(parent){if(this!=x3dom.nodeTypes.Appearance._defaultNode){parent.setAppDirty();}}}));x3dom.nodeTypes.Appearance.defaultNode=function(){if(!x3dom.nodeTypes.Appearance._defaultNode){x3dom.nodeTypes.Appearance._defaultNode=new x3dom.nodeTypes.Appearance();x3dom.nodeTypes.Appearance._defaultNode.nodeChanged();}
return x3dom.nodeTypes.Appearance._defaultNode;};x3dom.registerNodeType("X3DAppearanceChildNode","Shape",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DAppearanceChildNode.superClass.call(this,ctx);}));x3dom.registerNodeType("BlendMode","Shape",defineClass(x3dom.nodeTypes.X3DAppearanceChildNode,function(ctx){x3dom.nodeTypes.BlendMode.superClass.call(this,ctx);this.addField_SFString(ctx,'srcFactor',"src_alpha");this.addField_SFString(ctx,'destFactor',"one_minus_src_alpha");this.addField_SFColor(ctx,'color',1,1,1);this.addField_SFFloat(ctx,'colorTransparency',0);this.addField_SFString(ctx,'alphaFunc',"none");this.addField_SFFloat(ctx,'alphaFuncValue',0);this.addField_SFString(ctx,'equation',"none");}));x3dom.registerNodeType("DepthMode","Shape",defineClass(x3dom.nodeTypes.X3DAppearanceChildNode,function(ctx){x3dom.nodeTypes.DepthMode.superClass.call(this,ctx);this.addField_SFBool(ctx,'enableDepthTest',true);this.addField_SFString(ctx,'depthFunc',"none");this.addField_SFBool(ctx,'readOnly',false);this.addField_SFFloat(ctx,'zNearRange',-1);this.addField_SFFloat(ctx,'zFarRange',-1);}));x3dom.registerNodeType("X3DMaterialNode","Shape",defineClass(x3dom.nodeTypes.X3DAppearanceChildNode,function(ctx){x3dom.nodeTypes.X3DMaterialNode.superClass.call(this,ctx);}));x3dom.registerNodeType("Material","Shape",defineClass(x3dom.nodeTypes.X3DMaterialNode,function(ctx){x3dom.nodeTypes.Material.superClass.call(this,ctx);this.addField_SFFloat(ctx,'ambientIntensity',0.2);this.addField_SFColor(ctx,'diffuseColor',0.8,0.8,0.8);this.addField_SFColor(ctx,'emissiveColor',0,0,0);this.addField_SFFloat(ctx,'shininess',0.2);this.addField_SFColor(ctx,'specularColor',0,0,0);this.addField_SFFloat(ctx,'transparency',0);},{fieldChanged:function(fieldName){if(fieldName=="ambientIntensity"||fieldName=="diffuseColor"||fieldName=="emissiveColor"||fieldName=="shininess"||fieldName=="specularColor"||fieldName=="transparency")
{Array.forEach(this._parentNodes,function(app){Array.forEach(app._parentNodes,function(shape){shape._dirty.material=true;});});}}}));x3dom.nodeTypes.Material.defaultNode=function(){if(!x3dom.nodeTypes.Material._defaultNode){x3dom.nodeTypes.Material._defaultNode=new x3dom.nodeTypes.Material();x3dom.nodeTypes.Material._defaultNode.nodeChanged();}
return x3dom.nodeTypes.Material._defaultNode;};x3dom.registerNodeType("X3DShapeNode","Shape",defineClass(x3dom.nodeTypes.X3DBoundedNode,function(ctx){x3dom.nodeTypes.X3DShapeNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'render',true);this.addField_SFBool(ctx,'isPickable',true);this.addField_SFVec3f(ctx,'bboxCenter',0,0,0);this.addField_SFVec3f(ctx,'bboxSize',-1,-1,-1);this.addField_SFNode('appearance',x3dom.nodeTypes.X3DAppearanceNode);this.addField_SFNode('geometry',x3dom.nodeTypes.X3DGeometryNode);this._objectID=0;this._cleanupGLObjects=null;this._dirty={positions:true,normals:true,texcoords:true,colors:true,indexes:true,texture:true,material:true,text:true,shader:true};this._coordStrideOffset=[0,0];this._normalStrideOffset=[0,0];this._texCoordStrideOffset=[0,0];this._colorStrideOffset=[0,0];},{collectDrawableObjects:function(transform,out)
{if(out&&this._vf.render&&this._cf.geometry.node)
{out.cnt++;out.push([transform,this]);}},transformMatrix:function(transform)
{{return transform;}},getVolume:function(min,max){if(this._cf.geometry.node){return this._cf.geometry.node.getVolume(min,max);}
else{return false;}},getCenter:function(){if(this._cf.geometry.node){return this._cf.geometry.node.getCenter();}
else{return new x3dom.fields.SFVec3f(0,0,0);}},getDiameter:function(){if(this._cf.geometry.node){return this._cf.geometry.node.getDiameter();}
else{return 0;}},doIntersect:function(line){return this._cf.geometry.node.doIntersect(line);},isSolid:function(){return this._cf.geometry.node._vf.solid;},isCCW:function(){return this._cf.geometry.node._vf.ccw;},parentRemoved:function(parent){for(var i=0,n=this._childNodes.length;i<n;i++){if(this._childNodes[i]){this._childNodes[i].parentRemoved(this);}}
if(this._cleanupGLObjects){this._cleanupGLObjects();}},unsetDirty:function(){this._dirty.positions=false;this._dirty.normals=false;this._dirty.texCoords=false;this._dirty.colors=false;this._dirty.indexes=false;this._dirty.texture=false;this._dirty.material=false;this._dirty.text=false;this._dirty.shader=false;},setAllDirty:function(){this._dirty.positions=true;this._dirty.normals=true;this._dirty.texCoords=true;this._dirty.colors=true;this._dirty.indexes=true;this._dirty.texture=true;this._dirty.material=true;this._dirty.text=true;this._dirty.shader=true;},setAppDirty:function(){this._dirty.texture=true;this._dirty.material=true;this._dirty.shader=true;},setGeoDirty:function(){this._dirty.positions=true;this._dirty.normals=true;this._dirty.texcoords=true;this._dirty.colors=true;this._dirty.indexes=true;},getTextures:function(){var textures=[];if(this._cf.appearance.node){var tex=this._cf.appearance.node._cf.texture.node;if(tex){if(x3dom.isa(tex,x3dom.nodeTypes.MultiTexture)){textures=textures.concat(tex.getTextures());}else{textures.push(tex);}}
var shader=this._cf.appearance.node._cf.shaders.nodes[0];if(shader){if(x3dom.isa(shader,x3dom.nodeTypes.CommonSurfaceShader)){textures=textures.concat(shader.getTextures());}}}
var geometry=this._cf.geometry.node;if(geometry){if(x3dom.isa(geometry,x3dom.nodeTypes.ImageGeometry)){textures=textures.concat(geometry.getTextures());}else if(x3dom.isa(geometry,x3dom.nodeTypes.Text)){textures=textures.concat(geometry);}}
return textures;}}));x3dom.registerNodeType("Shape","Shape",defineClass(x3dom.nodeTypes.X3DShapeNode,function(ctx){x3dom.nodeTypes.Shape.superClass.call(this,ctx);},{nodeChanged:function(){if(!this._cf.appearance.node){}
if(!this._cf.geometry.node){if(this._DEF)
x3dom.debug.logError("No geometry given in Shape/"+this._DEF);}
else if(!this._objectID&&this._cf.geometry.node._pickable){this._objectID=++x3dom.nodeTypes.Shape.objectID;x3dom.nodeTypes.Shape.idMap.nodeID[this._objectID]=this;}}}));x3dom.nodeTypes.Shape.objectID=0;x3dom.nodeTypes.Shape.idMap={nodeID:{},remove:function(obj){for(var prop in this.nodeID){if(this.nodeID.hasOwnProperty(prop)){var val=this.nodeID[prop];if(val._objectID&&obj._objectID&&val._objectID===obj._objectID)
{delete this.nodeID[prop];x3dom.debug.logInfo("Unreg "+val._objectID);}}}}};x3dom.registerNodeType("X3DLightNode","Lighting",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DLightNode.superClass.call(this,ctx);if(ctx)
ctx.doc._nodeBag.lights.push(this);else
x3dom.debug.logWarning("X3DLightNode: No runtime context found!");this._lightID=0;this._dirty=true;this.addField_SFFloat(ctx,'ambientIntensity',0);this.addField_SFColor(ctx,'color',1,1,1);this.addField_SFFloat(ctx,'intensity',1);this.addField_SFBool(ctx,'global',false);this.addField_SFBool(ctx,'on',true);this.addField_SFFloat(ctx,'shadowIntensity',0);},{getViewMatrix:function(vec){return x3dom.fields.SFMatrix4f.identity;},nodeChanged:function(){if(!this._lightID){this._lightID=++x3dom.nodeTypes.X3DLightNode.lightID;}},fieldChanged:function(fieldName)
{this._dirty=true;},parentRemoved:function(parent)
{if(this._parentNodes.length===0){var doc=this.findX3DDoc();for(var i=0,n=doc._nodeBag.lights.length;i<n;i++){if(doc._nodeBag.lights[i]===this){doc._nodeBag.lights.splice(i,1);}}}}}));x3dom.nodeTypes.X3DLightNode.lightID=0;x3dom.registerNodeType("DirectionalLight","Lighting",defineClass(x3dom.nodeTypes.X3DLightNode,function(ctx){x3dom.nodeTypes.DirectionalLight.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'direction',0,0,-1);},{getViewMatrix:function(vec){var dir=this._vf.direction.normalize();var orientation=x3dom.fields.Quaternion.rotateFromTo(new x3dom.fields.SFVec3f(0,0,-1),dir);return orientation.toMatrix().transpose().mult(x3dom.fields.SFMatrix4f.translation(vec.negate()));}}));x3dom.registerNodeType("PointLight","Lighting",defineClass(x3dom.nodeTypes.X3DLightNode,function(ctx){x3dom.nodeTypes.PointLight.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'attenuation',1,0,0);this.addField_SFVec3f(ctx,'location',0,0,0);this.addField_SFFloat(ctx,'radius',100);this._vf.global=true;},{getViewMatrix:function(vec){var pos=this._vf.location;var orientation=x3dom.fields.Quaternion.rotateFromTo(new x3dom.fields.SFVec3f(0,0,-1),vec);return orientation.toMatrix().transpose().mult(x3dom.fields.SFMatrix4f.translation(pos.negate()));}}));x3dom.registerNodeType("SpotLight","Lighting",defineClass(x3dom.nodeTypes.X3DLightNode,function(ctx){x3dom.nodeTypes.SpotLight.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'direction',0,0,-1);this.addField_SFVec3f(ctx,'attenuation',1,0,0);this.addField_SFVec3f(ctx,'location',0,0,0);this.addField_SFFloat(ctx,'radius',100);this.addField_SFFloat(ctx,'beamWidth',1.5707963);this.addField_SFFloat(ctx,'cutOffAngle',1.5707963);this._vf.global=true;},{getViewMatrix:function(vec){var pos=this._vf.location;var dir=this._vf.direction.normalize();var orientation=x3dom.fields.Quaternion.rotateFromTo(new x3dom.fields.SFVec3f(0,0,-1),dir);return orientation.toMatrix().transpose().mult(x3dom.fields.SFMatrix4f.translation(pos.negate()));}}));x3dom.registerNodeType("X3DFollowerNode","Followers",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DFollowerNode.superClass.call(this,ctx);if(ctx)
ctx.doc._nodeBag.followers.push(this);else
x3dom.debug.logWarning("X3DFollowerNode: No runtime context found!");this.addField_SFBool(ctx,'isActive',false);},{nodeChanged:function(){},fieldChanged:function(fieldName){},parentRemoved:function(parent)
{if(this._parentNodes.length===0){var doc=this.findX3DDoc();for(var i=0,n=doc._nodeBag.followers.length;i<n;i++){if(doc._nodeBag.followers[i]===this){doc._nodeBag.followers.splice(i,1);}}}},tick:function(t){return false;},stepResponse:function(t)
{if(t<=0){return 0;}
if(t>=this._vf.duration){return 1;}
return this.stepResponseCore(t/this._vf.duration);},stepResponseCore:function(T)
{return 0.5-0.5*Math.cos(T*Math.PI);}}));x3dom.registerNodeType("X3DChaserNode","Followers",defineClass(x3dom.nodeTypes.X3DFollowerNode,function(ctx){x3dom.nodeTypes.X3DChaserNode.superClass.call(this,ctx);this.addField_SFTime(ctx,'duration',0);this._initDone=false;this._stepTime=0;this._currTime=0;this._bufferEndTime=0;this._numSupports=60;},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("X3DDamperNode","Followers",defineClass(x3dom.nodeTypes.X3DFollowerNode,function(ctx){x3dom.nodeTypes.X3DDamperNode.superClass.call(this,ctx);this.addField_SFTime(ctx,'tau',0);this.addField_SFFloat(ctx,'tolerance',-1);this.addField_SFInt32(ctx,'order',0);this._eps=this._vf.tolerance<0?0.001:this._vf.tolerance;this._lastTick=0;},{nodeChanged:function(){},fieldChanged:function(fieldName)
{if(fieldName==="tolerance")
{this._eps=this._vf.tolerance<0?0.001:this._vf.tolerance;}}}));x3dom.registerNodeType("ColorChaser","Followers",defineClass(x3dom.nodeTypes.X3DChaserNode,function(ctx){x3dom.nodeTypes.ColorChaser.superClass.call(this,ctx);this.addField_SFColor(ctx,'initialDestination',0.8,0.8,0.8);this.addField_SFColor(ctx,'initialValue',0.8,0.8,0.8);this.addField_SFColor(ctx,'set_value',0,0,0);this.addField_SFColor(ctx,'set_destination',0,0,0);this._buffer=new x3dom.fields.MFColor();this._previousValue=new x3dom.fields.SFColor(0,0,0);this._value=new x3dom.fields.SFColor(0,0,0);},{nodeChanged:function()
{this.initialize();},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{this.initialize();this.updateBuffer(this._currTime);if(!this._vf.isActive){this.postMessage('isActive',true);}}
else if(fieldName.indexOf("set_value")>=0)
{this.initialize();this._previousValue.setValues(this._vf.set_value);for(var C=1;C<this._buffer.length;C++){this._buffer[C].setValues(this._vf.set_value);}
this.postMessage('value_changed',this._vf.set_value);if(!this._vf.isActive){this.postMessage('isActive',true);}}},initialize:function()
{if(!this._initDone)
{this._initDone=true;this._vf.set_destination=this._vf.initialDestination;this._buffer.length=this._numSupports;this._buffer[0]=this._vf.initialDestination;for(var C=1;C<this._buffer.length;C++){this._buffer[C]=this._vf.initialValue;}
this._previousValue=this._vf.initialValue;this._stepTime=this._vf.duration/this._numSupports;var active=!this._buffer[0].equals(this._buffer[1],x3dom.fields.Eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}}},tick:function(now)
{this.initialize();this._currTime=now;if(!this._bufferEndTime)
{this._bufferEndTime=now;this._value=this._vf.initialValue;this.postMessage('value_changed',this._value);return true;}
var Frac=this.updateBuffer(now);var Output=this._previousValue;var DeltaIn=this._buffer[this._buffer.length-1].subtract(this._previousValue);var DeltaOut=DeltaIn.multiply(this.stepResponse((this._buffer.length-1+Frac)*this._stepTime));Output=Output.add(DeltaOut);for(var C=this._buffer.length-2;C>=0;C--)
{DeltaIn=this._buffer[C].subtract(this._buffer[C+1]);DeltaOut=DeltaIn.multiply(this.stepResponse((C+Frac)*this._stepTime));Output=Output.add(DeltaOut);}
if(!Output.equals(this._value,x3dom.fields.Eps)){this._value.setValues(Output);this.postMessage('value_changed',this._value);}
else{this.postMessage('isActive',false);}
return this._vf.isActive;},updateBuffer:function(now)
{var Frac=(now-this._bufferEndTime)/this._stepTime;var C;var NumToShift;var Alpha;if(Frac>=1)
{NumToShift=Math.floor(Frac);Frac-=NumToShift;if(NumToShift<this._buffer.length)
{this._previousValue=this._buffer[this._buffer.length-NumToShift];for(C=this._buffer.length-1;C>=NumToShift;C--){this._buffer[C]=this._buffer[C-NumToShift];}
for(C=0;C<NumToShift;C++)
{Alpha=C/NumToShift;this._buffer[C]=this._buffer[NumToShift].multiply(Alpha).add(this._vf.set_destination.multiply((1-Alpha)));}}
else
{this._previousValue=(NumToShift==this._buffer.length)?this._buffer[0]:this._vf.set_destination;for(C=0;C<this._buffer.length;C++){this._buffer[C]=this._vf.set_destination;}}
this._bufferEndTime+=NumToShift*this._stepTime;}
return Frac;}}));x3dom.registerNodeType("ColorDamper","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.ColorDamper.superClass.call(this,ctx);this.addField_SFColor(ctx,'initialDestination',0.8,0.8,0.8);this.addField_SFColor(ctx,'initialValue',0.8,0.8,0.8);this.addField_SFColor(ctx,'set_value',0,0,0);this.addField_SFColor(ctx,'set_destination',0,0,0);this._value0=new x3dom.fields.SFColor(0,0,0);this._value1=new x3dom.fields.SFColor(0,0,0);this._value2=new x3dom.fields.SFColor(0,0,0);this._value3=new x3dom.fields.SFColor(0,0,0);this._value4=new x3dom.fields.SFColor(0,0,0);this._value5=new x3dom.fields.SFColor(0,0,0);this.initialize();},{nodeChanged:function()
{},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{if(!this._value0.equals(this._vf.set_destination,this._eps)){this._value0=this._vf.set_destination;if(!this._vf.isActive){this.postMessage('isActive',true);}}}
else if(fieldName.indexOf("set_value")>=0)
{this._value1.setValues(this._vf.set_value);this._value2.setValues(this._vf.set_value);this._value3.setValues(this._vf.set_value);this._value4.setValues(this._vf.set_value);this._value5.setValues(this._vf.set_value);this._lastTick=0;this.postMessage('value_changed',this._value5);if(!this._vf.isActive){this._lastTick=0;this.postMessage('isActive',true);}}},initialize:function()
{this._value0.setValues(this._vf.initialDestination);this._value1.setValues(this._vf.initialValue);this._value2.setValues(this._vf.initialValue);this._value3.setValues(this._vf.initialValue);this._value4.setValues(this._vf.initialValue);this._value5.setValues(this._vf.initialValue);this._lastTick=0;var active=!this._value0.equals(this._value1,this._eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}},distance:function(a,b)
{var diff=a.subtract(b);return Math.sqrt(diff.r*diff.r+diff.g*diff.g+diff.b*diff.b);},tick:function(now)
{if(!this._lastTick)
{this._lastTick=now;return false;}
var delta=now-this._lastTick;var alpha=Math.exp(-delta/this._vf.tau);this._value1=this._vf.order>0&&this._vf.tau?this._value0.add(this._value1.subtract(this._value0).multiply(alpha)):new x3dom.fields.SFColor(this._value0.r,this._value0.g,this._value0.b);this._value2=this._vf.order>1&&this._vf.tau?this._value1.add(this._value2.subtract(this._value1).multiply(alpha)):new x3dom.fields.SFColor(this._value1.r,this._value1.g,this._value1.b);this._value3=this._vf.order>2&&this._vf.tau?this._value2.add(this._value3.subtract(this._value2).multiply(alpha)):new x3dom.fields.SFColor(this._value2.r,this._value2.g,this._value2.b);this._value4=this._vf.order>3&&this._vf.tau?this._value3.add(this._value4.subtract(this._value3).multiply(alpha)):new x3dom.fields.SFColor(this._value3.r,this._value3.g,this._value3.b);this._value5=this._vf.order>4&&this._vf.tau?this._value4.add(this._value5.subtract(this._value4).multiply(alpha)):new x3dom.fields.SFColor(this._value4.r,this._value4.g,this._value4.b);var dist=this.distance(this._value1,this._value0);if(this._vf.order>1)
{var dist2=this.distance(this._value2,this._value1);if(dist2>dist){dist=dist2;}}
if(this._vf.order>2)
{var dist3=this.distance(this._value3,this._value2);if(dist3>dist){dist=dist3;}}
if(this._vf.order>3)
{var dist4=this.distance(this._value4,this._value3);if(dist4>dist){dist=dist4;}}
if(this._vf.order>4)
{var dist5=this.distance(this._value5,this._value4);if(dist5>dist){dist=dist5;}}
if(dist<this._eps)
{this._value1.setValues(this._value0);this._value2.setValues(this._value0);this._value3.setValues(this._value0);this._value4.setValues(this._value0);this._value5.setValues(this._value0);this.postMessage('value_changed',this._value0);this.postMessage('isActive',false);this._lastTick=0;return false;}
this.postMessage('value_changed',this._value5);this._lastTick=now;return true;}}));x3dom.registerNodeType("OrientationChaser","Followers",defineClass(x3dom.nodeTypes.X3DChaserNode,function(ctx){x3dom.nodeTypes.OrientationChaser.superClass.call(this,ctx);this.addField_SFRotation(ctx,'initialDestination',0,1,0,0);this.addField_SFRotation(ctx,'initialValue',0,1,0,0);this.addField_SFRotation(ctx,'set_value',0,1,0,0);this.addField_SFRotation(ctx,'set_destination',0,1,0,0);this._numSupports=30;this._buffer=new x3dom.fields.MFRotation();this._previousValue=new x3dom.fields.Quaternion(0,1,0,0);this._value=new x3dom.fields.Quaternion(0,1,0,0);},{nodeChanged:function()
{this.initialize();},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{this.initialize();this.updateBuffer(this._currTime);if(!this._vf.isActive){this.postMessage('isActive',true);}}
else if(fieldName.indexOf("set_value")>=0)
{this.initialize();this._previousValue.setValues(this._vf.set_value);for(var C=1;C<this._buffer.length;C++){this._buffer[C].setValues(this._vf.set_value);}
this.postMessage('value_changed',this._vf.set_value);if(!this._vf.isActive){this.postMessage('isActive',true);}}},initialize:function()
{if(!this._initDone)
{this._initDone=true;this._vf.set_destination=this._vf.initialDestination;this._buffer.length=this._numSupports;this._buffer[0]=this._vf.initialDestination;for(var C=1;C<this._buffer.length;C++){this._buffer[C]=this._vf.initialValue;}
this._previousValue=this._vf.initialValue;this._stepTime=this._vf.duration/this._numSupports;var active=!this._buffer[0].equals(this._buffer[1],x3dom.fields.Eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}}},tick:function(now)
{this.initialize();this._currTime=now;if(!this._bufferEndTime)
{this._bufferEndTime=now;this._value=this._vf.initialValue;this.postMessage('value_changed',this._value);return true;}
var Frac=this.updateBuffer(now);var Output=this._previousValue;var DeltaIn=this._previousValue.inverse().multiply(this._buffer[this._buffer.length-1]);Output=Output.slerp(Output.multiply(DeltaIn),this.stepResponse((this._buffer.length-1+Frac)*this._stepTime));for(var C=this._buffer.length-2;C>=0;C--)
{DeltaIn=this._buffer[C+1].inverse().multiply(this._buffer[C]);Output=Output.slerp(Output.multiply(DeltaIn),this.stepResponse((C+Frac)*this._stepTime));}
if(!Output.equals(this._value,x3dom.fields.Eps)){Output=Output.normalize(Output);this._value.setValues(Output);this.postMessage('value_changed',this._value);}
else{this.postMessage('isActive',false);}
return this._vf.isActive;},updateBuffer:function(now)
{var Frac=(now-this._bufferEndTime)/this._stepTime;var C;var NumToShift;var Alpha;if(Frac>=1)
{NumToShift=Math.floor(Frac);Frac-=NumToShift;if(NumToShift<this._buffer.length)
{this._previousValue=this._buffer[this._buffer.length-NumToShift];for(C=this._buffer.length-1;C>=NumToShift;C--){this._buffer[C]=this._buffer[C-NumToShift];}
for(C=0;C<NumToShift;C++)
{Alpha=C/NumToShift;this._buffer[C]=this._vf.set_destination.slerp(this._buffer[NumToShift],Alpha);}}
else
{this._previousValue=(NumToShift==this._buffer.length)?this._buffer[0]:this._vf.set_destination;for(C=0;C<this._buffer.length;C++){this._buffer[C]=this._vf.set_destination;}}
this._bufferEndTime+=NumToShift*this._stepTime;}
return Frac;}}));x3dom.registerNodeType("OrientationDamper","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.OrientationDamper.superClass.call(this,ctx);this.addField_SFRotation(ctx,'initialDestination',0,1,0,0);this.addField_SFRotation(ctx,'initialValue',0,1,0,0);this.addField_SFRotation(ctx,'set_value',0,1,0,0);this.addField_SFRotation(ctx,'set_destination',0,1,0,0);this._value0=new x3dom.fields.Quaternion(0,1,0,0);this._value1=new x3dom.fields.Quaternion(0,1,0,0);this._value2=new x3dom.fields.Quaternion(0,1,0,0);this._value3=new x3dom.fields.Quaternion(0,1,0,0);this._value4=new x3dom.fields.Quaternion(0,1,0,0);this._value5=new x3dom.fields.Quaternion(0,1,0,0);this.initialize();},{nodeChanged:function()
{},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{if(!this._value0.equals(this._vf.set_destination,this._eps)){this._value0=this._vf.set_destination;if(!this._vf.isActive){this.postMessage('isActive',true);}}}
else if(fieldName.indexOf("set_value")>=0)
{this._value1.setValues(this._vf.set_value);this._value2.setValues(this._vf.set_value);this._value3.setValues(this._vf.set_value);this._value4.setValues(this._vf.set_value);this._value5.setValues(this._vf.set_value);this._lastTick=0;this.postMessage('value_changed',this._value5);if(!this._vf.isActive){this._lastTick=0;this.postMessage('isActive',true);}}},initialize:function()
{this._value0.setValues(this._vf.initialDestination);this._value1.setValues(this._vf.initialValue);this._value2.setValues(this._vf.initialValue);this._value3.setValues(this._vf.initialValue);this._value4.setValues(this._vf.initialValue);this._value5.setValues(this._vf.initialValue);this._lastTick=0;var active=!this._value0.equals(this._value1,this._eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}},tick:function(now)
{if(!this._lastTick)
{this._lastTick=now;return false;}
var delta=now-this._lastTick;var alpha=Math.exp(-delta/this._vf.tau);this._value1=this._vf.order>0&&this._vf.tau?this._value0.slerp(this._value1,alpha):new x3dom.fields.Quaternion(this._value0.x,this._value0.y,this._value0.z,this._value0.w);this._value2=this._vf.order>1&&this._vf.tau?this._value1.slerp(this._value2,alpha):new x3dom.fields.Quaternion(this._value1.x,this._value1.y,this._value1.z,this._value1.w);this._value3=this._vf.order>2&&this._vf.tau?this._value2.slerp(this._value3,alpha):new x3dom.fields.Quaternion(this._value2.x,this._value2.y,this._value2.z,this._value2.w);this._value4=this._vf.order>3&&this._vf.tau?this._value3.slerp(this._value4,alpha):new x3dom.fields.Quaternion(this._value3.x,this._value3.y,this._value3.z,this._value3.w);this._value5=this._vf.order>4&&this._vf.tau?this._value4.slerp(this._value5,alpha):new x3dom.fields.Quaternion(this._value4.x,this._value4.y,this._value4.z,this._value4.w);var dist=Math.abs(this._value1.inverse().multiply(this._value0).angle());if(this._vf.order>1)
{var dist2=Math.abs(this._value2.inverse().multiply(this._value1).angle());if(dist2>dist){dist=dist2;}}
if(this._vf.order>2)
{var dist3=Math.abs(this._value3.inverse().multiply(this._value2).angle());if(dist3>dist){dist=dist3;}}
if(this._vf.order>3)
{var dist4=Math.abs(this._value4.inverse().multiply(this._value3).angle());if(dist4>dist){dist=dist4;}}
if(this._vf.order>4)
{var dist5=Math.abs(this._value5.inverse().multiply(this._value4).angle());if(dist5>dist){dist=dist5;}}
if(dist<this._eps)
{this._value1.setValues(this._value0);this._value2.setValues(this._value0);this._value3.setValues(this._value0);this._value4.setValues(this._value0);this._value5.setValues(this._value0);this.postMessage('value_changed',this._value0);this.postMessage('isActive',false);this._lastTick=0;return false;}
this.postMessage('value_changed',this._value5);this._lastTick=now;return true;}}));x3dom.registerNodeType("PositionChaser","Followers",defineClass(x3dom.nodeTypes.X3DChaserNode,function(ctx){x3dom.nodeTypes.PositionChaser.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'initialDestination',0,0,0);this.addField_SFVec3f(ctx,'initialValue',0,0,0);this.addField_SFVec3f(ctx,'set_value',0,0,0);this.addField_SFVec3f(ctx,'set_destination',0,0,0);this._buffer=new x3dom.fields.MFVec3f();this._previousValue=new x3dom.fields.SFVec3f(0,0,0);this._value=new x3dom.fields.SFVec3f(0,0,0);},{nodeChanged:function()
{this.initialize();},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{this.initialize();this.updateBuffer(this._currTime);if(!this._vf.isActive){this.postMessage('isActive',true);}}
else if(fieldName.indexOf("set_value")>=0)
{this.initialize();this._previousValue.setValues(this._vf.set_value);for(var C=1;C<this._buffer.length;C++){this._buffer[C].setValues(this._vf.set_value);}
this.postMessage('value_changed',this._vf.set_value);if(!this._vf.isActive){this.postMessage('isActive',true);}}},initialize:function()
{if(!this._initDone)
{this._initDone=true;this._vf.set_destination=this._vf.initialDestination;this._buffer.length=this._numSupports;this._buffer[0]=this._vf.initialDestination;for(var C=1;C<this._buffer.length;C++){this._buffer[C]=this._vf.initialValue;}
this._previousValue=this._vf.initialValue;this._stepTime=this._vf.duration/this._numSupports;var active=!this._buffer[0].equals(this._buffer[1],x3dom.fields.Eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}}},tick:function(now)
{this.initialize();this._currTime=now;if(!this._bufferEndTime)
{this._bufferEndTime=now;this._value=this._vf.initialValue;this.postMessage('value_changed',this._value);return true;}
var Frac=this.updateBuffer(now);var Output=this._previousValue;var DeltaIn=this._buffer[this._buffer.length-1].subtract(this._previousValue);var DeltaOut=DeltaIn.multiply(this.stepResponse((this._buffer.length-1+Frac)*this._stepTime));Output=Output.add(DeltaOut);for(var C=this._buffer.length-2;C>=0;C--)
{DeltaIn=this._buffer[C].subtract(this._buffer[C+1]);DeltaOut=DeltaIn.multiply(this.stepResponse((C+Frac)*this._stepTime));Output=Output.add(DeltaOut);}
if(!Output.equals(this._value,x3dom.fields.Eps)){this._value.setValues(Output);this.postMessage('value_changed',this._value);}
else{this.postMessage('isActive',false);}
return this._vf.isActive;},updateBuffer:function(now)
{var Frac=(now-this._bufferEndTime)/this._stepTime;var C;var NumToShift;var Alpha;if(Frac>=1)
{NumToShift=Math.floor(Frac);Frac-=NumToShift;if(NumToShift<this._buffer.length)
{this._previousValue=this._buffer[this._buffer.length-NumToShift];for(C=this._buffer.length-1;C>=NumToShift;C--){this._buffer[C]=this._buffer[C-NumToShift];}
for(C=0;C<NumToShift;C++)
{Alpha=C/NumToShift;this._buffer[C]=this._buffer[NumToShift].multiply(Alpha).add(this._vf.set_destination.multiply((1-Alpha)));}}
else
{this._previousValue=(NumToShift==this._buffer.length)?this._buffer[0]:this._vf.set_destination;for(C=0;C<this._buffer.length;C++){this._buffer[C]=this._vf.set_destination;}}
this._bufferEndTime+=NumToShift*this._stepTime;}
return Frac;}}));x3dom.registerNodeType("PositionChaser2D","Followers",defineClass(x3dom.nodeTypes.X3DChaserNode,function(ctx){x3dom.nodeTypes.PositionChaser2D.superClass.call(this,ctx);this.addField_SFVec2f(ctx,'initialDestination',0,0);this.addField_SFVec2f(ctx,'initialValue',0,0);this.addField_SFVec2f(ctx,'set_value',0,0);this.addField_SFVec2f(ctx,'set_destination',0,0);this._buffer=new x3dom.fields.MFVec2f();this._previousValue=new x3dom.fields.SFVec2f(0,0);this._value=new x3dom.fields.SFVec2f(0,0);},{nodeChanged:function()
{this.initialize();},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{this.initialize();this.updateBuffer(this._currTime);if(!this._vf.isActive){this.postMessage('isActive',true);}}
else if(fieldName.indexOf("set_value")>=0)
{this.initialize();this._previousValue.setValues(this._vf.set_value);for(var C=1;C<this._buffer.length;C++){this._buffer[C].setValues(this._vf.set_value);}
this.postMessage('value_changed',this._vf.set_value);if(!this._vf.isActive){this.postMessage('isActive',true);}}},initialize:function()
{if(!this._initDone)
{this._initDone=true;this._vf.set_destination=this._vf.initialDestination;this._buffer.length=this._numSupports;this._buffer[0]=this._vf.initialDestination;for(var C=1;C<this._buffer.length;C++){this._buffer[C]=this._vf.initialValue;}
this._previousValue=this._vf.initialValue;this._stepTime=this._vf.duration/this._numSupports;var active=!this._buffer[0].equals(this._buffer[1],x3dom.fields.Eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}}},tick:function(now)
{this.initialize();this._currTime=now;if(!this._bufferEndTime)
{this._bufferEndTime=now;this._value=this._vf.initialValue;this.postMessage('value_changed',this._value);return true;}
var Frac=this.updateBuffer(now);var Output=this._previousValue;var DeltaIn=this._buffer[this._buffer.length-1].subtract(this._previousValue);var DeltaOut=DeltaIn.multiply(this.stepResponse((this._buffer.length-1+Frac)*this._stepTime));Output=Output.add(DeltaOut);for(var C=this._buffer.length-2;C>=0;C--)
{DeltaIn=this._buffer[C].subtract(this._buffer[C+1]);DeltaOut=DeltaIn.multiply(this.stepResponse((C+Frac)*this._stepTime));Output=Output.add(DeltaOut);}
if(!Output.equals(this._value,x3dom.fields.Eps)){this._value.setValues(Output);this.postMessage('value_changed',this._value);}
else{this.postMessage('isActive',false);}
return this._vf.isActive;},updateBuffer:function(now)
{var Frac=(now-this._bufferEndTime)/this._stepTime;var C;var NumToShift;var Alpha;if(Frac>=1)
{NumToShift=Math.floor(Frac);Frac-=NumToShift;if(NumToShift<this._buffer.length)
{this._previousValue=this._buffer[this._buffer.length-NumToShift];for(C=this._buffer.length-1;C>=NumToShift;C--){this._buffer[C]=this._buffer[C-NumToShift];}
for(C=0;C<NumToShift;C++)
{Alpha=C/NumToShift;this._buffer[C]=this._buffer[NumToShift].multiply(Alpha).add(this._vf.set_destination.multiply((1-Alpha)));}}
else
{this._previousValue=(NumToShift==this._buffer.length)?this._buffer[0]:this._vf.set_destination;for(C=0;C<this._buffer.length;C++){this._buffer[C]=this._vf.set_destination;}}
this._bufferEndTime+=NumToShift*this._stepTime;}
return Frac;}}));x3dom.registerNodeType("PositionDamper","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.PositionDamper.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'initialDestination',0,0,0);this.addField_SFVec3f(ctx,'initialValue',0,0,0);this.addField_SFVec3f(ctx,'set_value',0,0,0);this.addField_SFVec3f(ctx,'set_destination',0,0,0);this._value0=new x3dom.fields.SFVec3f(0,0,0);this._value1=new x3dom.fields.SFVec3f(0,0,0);this._value2=new x3dom.fields.SFVec3f(0,0,0);this._value3=new x3dom.fields.SFVec3f(0,0,0);this._value4=new x3dom.fields.SFVec3f(0,0,0);this._value5=new x3dom.fields.SFVec3f(0,0,0);this.initialize();},{nodeChanged:function()
{},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{if(!this._value0.equals(this._vf.set_destination,this._eps)){this._value0=this._vf.set_destination;if(!this._vf.isActive){this.postMessage('isActive',true);}}}
else if(fieldName.indexOf("set_value")>=0)
{this._value1.setValues(this._vf.set_value);this._value2.setValues(this._vf.set_value);this._value3.setValues(this._vf.set_value);this._value4.setValues(this._vf.set_value);this._value5.setValues(this._vf.set_value);this._lastTick=0;this.postMessage('value_changed',this._value5);if(!this._vf.isActive){this._lastTick=0;this.postMessage('isActive',true);}}},initialize:function()
{this._value0.setValues(this._vf.initialDestination);this._value1.setValues(this._vf.initialValue);this._value2.setValues(this._vf.initialValue);this._value3.setValues(this._vf.initialValue);this._value4.setValues(this._vf.initialValue);this._value5.setValues(this._vf.initialValue);this._lastTick=0;var active=!this._value0.equals(this._value1,this._eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}},tick:function(now)
{if(!this._lastTick)
{this._lastTick=now;return false;}
var delta=now-this._lastTick;var alpha=Math.exp(-delta/this._vf.tau);this._value1=this._vf.order>0&&this._vf.tau?this._value0.add(this._value1.subtract(this._value0).multiply(alpha)):new x3dom.fields.SFVec3f(this._value0.x,this._value0.y,this._value0.z);this._value2=this._vf.order>1&&this._vf.tau?this._value1.add(this._value2.subtract(this._value1).multiply(alpha)):new x3dom.fields.SFVec3f(this._value1.x,this._value1.y,this._value1.z);this._value3=this._vf.order>2&&this._vf.tau?this._value2.add(this._value3.subtract(this._value2).multiply(alpha)):new x3dom.fields.SFVec3f(this._value2.x,this._value2.y,this._value2.z);this._value4=this._vf.order>3&&this._vf.tau?this._value3.add(this._value4.subtract(this._value3).multiply(alpha)):new x3dom.fields.SFVec3f(this._value3.x,this._value3.y,this._value3.z);this._value5=this._vf.order>4&&this._vf.tau?this._value4.add(this._value5.subtract(this._value4).multiply(alpha)):new x3dom.fields.SFVec3f(this._value4.x,this._value4.y,this._value4.z);var dist=this._value1.subtract(this._value0).length();if(this._vf.order>1)
{var dist2=this._value2.subtract(this._value1).length();if(dist2>dist){dist=dist2;}}
if(this._vf.order>2)
{var dist3=this._value3.subtract(this._value2).length();if(dist3>dist){dist=dist3;}}
if(this._vf.order>3)
{var dist4=this._value4.subtract(this._value3).length();if(dist4>dist){dist=dist4;}}
if(this._vf.order>4)
{var dist5=this._value5.subtract(this._value4).length();if(dist5>dist){dist=dist5;}}
if(dist<this._eps)
{this._value1.setValues(this._value0);this._value2.setValues(this._value0);this._value3.setValues(this._value0);this._value4.setValues(this._value0);this._value5.setValues(this._value0);this.postMessage('value_changed',this._value0);this.postMessage('isActive',false);this._lastTick=0;return false;}
this.postMessage('value_changed',this._value5);this._lastTick=now;return true;}}));x3dom.registerNodeType("PositionDamper2D","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.PositionDamper2D.superClass.call(this,ctx);this.addField_SFVec2f(ctx,'initialDestination',0,0);this.addField_SFVec2f(ctx,'initialValue',0,0);this.addField_SFVec2f(ctx,'set_value',0,0);this.addField_SFVec2f(ctx,'set_destination',0,0);this._value0=new x3dom.fields.SFVec2f(0,0);this._value1=new x3dom.fields.SFVec2f(0,0);this._value2=new x3dom.fields.SFVec2f(0,0);this._value3=new x3dom.fields.SFVec2f(0,0);this._value4=new x3dom.fields.SFVec2f(0,0);this._value5=new x3dom.fields.SFVec2f(0,0);this.initialize();},{nodeChanged:function()
{},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{if(!this._value0.equals(this._vf.set_destination,this._eps)){this._value0=this._vf.set_destination;if(!this._vf.isActive){this.postMessage('isActive',true);}}}
else if(fieldName.indexOf("set_value")>=0)
{this._value1.setValues(this._vf.set_value);this._value2.setValues(this._vf.set_value);this._value3.setValues(this._vf.set_value);this._value4.setValues(this._vf.set_value);this._value5.setValues(this._vf.set_value);this._lastTick=0;this.postMessage('value_changed',this._value5);if(!this._vf.isActive){this._lastTick=0;this.postMessage('isActive',true);}}},initialize:function()
{this._value0.setValues(this._vf.initialDestination);this._value1.setValues(this._vf.initialValue);this._value2.setValues(this._vf.initialValue);this._value3.setValues(this._vf.initialValue);this._value4.setValues(this._vf.initialValue);this._value5.setValues(this._vf.initialValue);this._lastTick=0;var active=!this._value0.equals(this._value1,this._eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}},tick:function(now)
{if(!this._lastTick)
{this._lastTick=now;return false;}
var delta=now-this._lastTick;var alpha=Math.exp(-delta/this._vf.tau);this._value1=this._vf.order>0&&this._vf.tau?this._value0.add(this._value1.subtract(this._value0).multiply(alpha)):new x3dom.fields.SFVec2f(this._value0.x,this._value0.y,this._value0.z);this._value2=this._vf.order>1&&this._vf.tau?this._value1.add(this._value2.subtract(this._value1).multiply(alpha)):new x3dom.fields.SFVec2f(this._value1.x,this._value1.y,this._value1.z);this._value3=this._vf.order>2&&this._vf.tau?this._value2.add(this._value3.subtract(this._value2).multiply(alpha)):new x3dom.fields.SFVec2f(this._value2.x,this._value2.y,this._value2.z);this._value4=this._vf.order>3&&this._vf.tau?this._value3.add(this._value4.subtract(this._value3).multiply(alpha)):new x3dom.fields.SFVec2f(this._value3.x,this._value3.y,this._value3.z);this._value5=this._vf.order>4&&this._vf.tau?this._value4.add(this._value5.subtract(this._value4).multiply(alpha)):new x3dom.fields.SFVec2f(this._value4.x,this._value4.y,this._value4.z);var dist=this._value1.subtract(this._value0).length();if(this._vf.order>1)
{var dist2=this._value2.subtract(this._value1).length();if(dist2>dist){dist=dist2;}}
if(this._vf.order>2)
{var dist3=this._value3.subtract(this._value2).length();if(dist3>dist){dist=dist3;}}
if(this._vf.order>3)
{var dist4=this._value4.subtract(this._value3).length();if(dist4>dist){dist=dist4;}}
if(this._vf.order>4)
{var dist5=this._value5.subtract(this._value4).length();if(dist5>dist){dist=dist5;}}
if(dist<this._eps)
{this._value1.setValues(this._value0);this._value2.setValues(this._value0);this._value3.setValues(this._value0);this._value4.setValues(this._value0);this._value5.setValues(this._value0);this.postMessage('value_changed',this._value0);this.postMessage('isActive',false);this._lastTick=0;return false;}
this.postMessage('value_changed',this._value5);this._lastTick=now;return true;}}));x3dom.registerNodeType("ScalarChaser","Followers",defineClass(x3dom.nodeTypes.X3DChaserNode,function(ctx){x3dom.nodeTypes.ScalarChaser.superClass.call(this,ctx);this.addField_SFFloat(ctx,'initialDestination',0);this.addField_SFFloat(ctx,'initialValue',0);this.addField_SFFloat(ctx,'set_value',0);this.addField_SFFloat(ctx,'set_destination',0);this._buffer=[];this._previousValue=0;this._value=0;},{nodeChanged:function()
{this.initialize();},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{this.initialize();this.updateBuffer(this._currTime);if(!this._vf.isActive){this.postMessage('isActive',true);}}
else if(fieldName.indexOf("set_value")>=0)
{this.initialize();this._previousValue=this._vf.set_value;for(var C=1;C<this._buffer.length;C++){this._buffer[C]=this._vf.set_value;}
this.postMessage('value_changed',this._vf.set_value);if(!this._vf.isActive){this.postMessage('isActive',true);}}},initialize:function()
{if(!this._initDone)
{this._initDone=true;this._vf.set_destination=this._vf.initialDestination;this._buffer.length=this._numSupports;this._buffer[0]=this._vf.initialDestination;for(var C=1;C<this._buffer.length;C++){this._buffer[C]=this._vf.initialValue;}
this._previousValue=this._vf.initialValue;this._stepTime=this._vf.duration/this._numSupports;var active=(Math.abs(this._buffer[0]-this._buffer[1])>=x3dom.fields.Eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}}},tick:function(now)
{this.initialize();this._currTime=now;if(!this._bufferEndTime)
{this._bufferEndTime=now;this._value=this._vf.initialValue;this.postMessage('value_changed',this._value);return true;}
var Frac=this.updateBuffer(now);var Output=this._previousValue;var DeltaIn=this._buffer[this._buffer.length-1]-this._previousValue;var DeltaOut=DeltaIn*(this.stepResponse((this._buffer.length-1+Frac)*this._stepTime));Output=Output+DeltaOut;for(var C=this._buffer.length-2;C>=0;C--)
{DeltaIn=this._buffer[C]-this._buffer[C+1];DeltaOut=DeltaIn*(this.stepResponse((C+Frac)*this._stepTime));Output=Output+DeltaOut;}
if(Math.abs(Output-this._value)>=x3dom.fields.Eps){this._value=Output;this.postMessage('value_changed',this._value);}
else{this.postMessage('isActive',false);}
return this._vf.isActive;},updateBuffer:function(now)
{var Frac=(now-this._bufferEndTime)/this._stepTime;var C;var NumToShift;var Alpha;if(Frac>=1)
{NumToShift=Math.floor(Frac);Frac-=NumToShift;if(NumToShift<this._buffer.length)
{this._previousValue=this._buffer[this._buffer.length-NumToShift];for(C=this._buffer.length-1;C>=NumToShift;C--){this._buffer[C]=this._buffer[C-NumToShift];}
for(C=0;C<NumToShift;C++)
{Alpha=C/NumToShift;this._buffer[C]=this._buffer[NumToShift]*Alpha+this._vf.set_destination*(1-Alpha);}}
else
{this._previousValue=(NumToShift==this._buffer.length)?this._buffer[0]:this._vf.set_destination;for(C=0;C<this._buffer.length;C++){this._buffer[C]=this._vf.set_destination;}}
this._bufferEndTime+=NumToShift*this._stepTime;}
return Frac;}}));x3dom.registerNodeType("ScalarDamper","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.ScalarDamper.superClass.call(this,ctx);this.addField_SFFloat(ctx,'initialDestination',0);this.addField_SFFloat(ctx,'initialValue',0);this.addField_SFFloat(ctx,'set_value',0);this.addField_SFFloat(ctx,'set_destination',0);this._value0=0;this._value1=0;this._value2=0;this._value3=0;this._value4=0;this._value5=0;this.initialize();},{nodeChanged:function()
{},fieldChanged:function(fieldName)
{if(fieldName.indexOf("set_destination")>=0)
{if(Math.abs(this._value0-this._vf.set_destination)>=this._eps){this._value0=this._vf.set_destination;if(!this._vf.isActive){this.postMessage('isActive',true);}}}
else if(fieldName.indexOf("set_value")>=0)
{this._value1=this._vf.set_value;this._value2=this._vf.set_value;this._value3=this._vf.set_value;this._value4=this._vf.set_value;this._value5=this._vf.set_value;this._lastTick=0;this.postMessage('value_changed',this._value5);if(!this._vf.isActive){this._lastTick=0;this.postMessage('isActive',true);}}},initialize:function()
{this._value0=this._vf.initialDestination;this._value1=this._vf.initialValue;this._value2=this._vf.initialValue;this._value3=this._vf.initialValue;this._value4=this._vf.initialValue;this._value5=this._vf.initialValue;this._lastTick=0;var active=(Math.abs(this._value0-this._value1)>=this._eps);if(this._vf.isActive!==active){this.postMessage('isActive',active);}},tick:function(now)
{if(!this._lastTick)
{this._lastTick=now;return false;}
var delta=now-this._lastTick;var alpha=Math.exp(-delta/this._vf.tau);this._value1=this._vf.order>0&&this._vf.tau?this._value0+alpha*(this._value1-this._value0):this._value0;this._value2=this._vf.order>1&&this._vf.tau?this._value1+alpha*(this._value2-this._value1):this._value1;this._value3=this._vf.order>2&&this._vf.tau?this._value2+alpha*(this._value3-this._value2):this._value2;this._value4=this._vf.order>3&&this._vf.tau?this._value3+alpha*(this._value4-this._value3):this._value3;this._value5=this._vf.order>4&&this._vf.tau?this._value4+alpha*(this._value5-this._value4):this._value4;var dist=Math.abs(this._value1-this._value0);if(this._vf.order>1)
{var dist2=Math.abs(this._value2-this._value1);if(dist2>dist){dist=dist2;}}
if(this._vf.order>2)
{var dist3=Math.abs(this._value3-this._value2);if(dist3>dist){dist=dist3;}}
if(this._vf.order>3)
{var dist4=Math.abs(this._value4-this._value3);if(dist4>dist){dist=dist4;}}
if(this._vf.order>4)
{var dist5=Math.abs(this._value5-this._value4);if(dist5>dist){dist=dist5;}}
if(dist<this._eps)
{this._value1=this._value0;this._value2=this._value0;this._value3=this._value0;this._value4=this._value0;this._value5=this._value0;this.postMessage('value_changed',this._value0);this.postMessage('isActive',false);this._lastTick=0;return false;}
this.postMessage('value_changed',this._value5);this._lastTick=now;return true;}}));x3dom.registerNodeType("CoordinateDamper","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.CoordinateDamper.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'initialDestination',[]);this.addField_MFVec3f(ctx,'initialValue',[]);this.addField_MFVec3f(ctx,'set_value',[]);this.addField_MFVec3f(ctx,'set_destination',[]);x3dom.debug.logWarning("CoordinateDamper NYI");},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("TexCoordDamper2D","Followers",defineClass(x3dom.nodeTypes.X3DDamperNode,function(ctx){x3dom.nodeTypes.TexCoordDamper2D.superClass.call(this,ctx);this.addField_MFVec2f(ctx,'initialDestination',[]);this.addField_MFVec2f(ctx,'initialValue',[]);this.addField_MFVec2f(ctx,'set_value',[]);this.addField_MFVec2f(ctx,'set_destination',[]);x3dom.debug.logWarning("TexCoordDamper2D NYI");},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("X3DInterpolatorNode","Interpolation",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DInterpolatorNode.superClass.call(this,ctx);this.addField_MFFloat(ctx,'key',[]);this.addField_SFFloat(ctx,'set_fraction',0);},{linearInterp:function(time,interp){if(time<=this._vf.key[0])
return this._vf.keyValue[0];else if(time>=this._vf.key[this._vf.key.length-1])
return this._vf.keyValue[this._vf.key.length-1];for(var i=0;i<this._vf.key.length-1;++i){if((this._vf.key[i]<time)&&(time<=this._vf.key[i+1]))
return interp(this._vf.keyValue[i],this._vf.keyValue[i+1],(time-this._vf.key[i])/(this._vf.key[i+1]-this._vf.key[i]));}
return this._vf.keyValue[0];}}));x3dom.registerNodeType("OrientationInterpolator","Interpolation",defineClass(x3dom.nodeTypes.X3DInterpolatorNode,function(ctx){x3dom.nodeTypes.OrientationInterpolator.superClass.call(this,ctx);this.addField_MFRotation(ctx,'keyValue',[]);},{fieldChanged:function(fieldName)
{if(fieldName==="set_fraction")
{var value=this.linearInterp(this._vf.set_fraction,function(a,b,t){return a.slerp(b,t);});this.postMessage('value_changed',value);}}}));x3dom.registerNodeType("PositionInterpolator","Interpolation",defineClass(x3dom.nodeTypes.X3DInterpolatorNode,function(ctx){x3dom.nodeTypes.PositionInterpolator.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'keyValue',[]);},{fieldChanged:function(fieldName)
{if(fieldName==="set_fraction")
{var value=this.linearInterp(this._vf.set_fraction,function(a,b,t){return a.multiply(1.0-t).add(b.multiply(t));});this.postMessage('value_changed',value);}}}));x3dom.registerNodeType("NormalInterpolator","Interpolation",defineClass(x3dom.nodeTypes.X3DInterpolatorNode,function(ctx){x3dom.nodeTypes.NormalInterpolator.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'keyValue',[]);},{fieldChanged:function(fieldName)
{if(fieldName==="set_fraction")
{var value=this.linearInterp(this._vf.set_fraction,function(a,b,t){return a.multiply(1.0-t).add(b.multiply(t)).normalize();});this.postMessage('value_changed',value);}}}));x3dom.registerNodeType("ColorInterpolator","Interpolation",defineClass(x3dom.nodeTypes.X3DInterpolatorNode,function(ctx){x3dom.nodeTypes.ColorInterpolator.superClass.call(this,ctx);this.addField_MFColor(ctx,'keyValue',[]);},{fieldChanged:function(fieldName)
{if(fieldName==="set_fraction")
{var value=this.linearInterp(this._vf.set_fraction,function(a,b,t){return a.multiply(1.0-t).add(b.multiply(t));});this.postMessage('value_changed',value);}}}));x3dom.registerNodeType("ScalarInterpolator","Interpolation",defineClass(x3dom.nodeTypes.X3DInterpolatorNode,function(ctx){x3dom.nodeTypes.ScalarInterpolator.superClass.call(this,ctx);this.addField_MFFloat(ctx,'keyValue',[]);},{fieldChanged:function(fieldName)
{if(fieldName==="set_fraction")
{var value=this.linearInterp(this._vf.set_fraction,function(a,b,t){return(1.0-t)*a+t*b;});this.postMessage('value_changed',value);}}}));x3dom.registerNodeType("CoordinateInterpolator","Interpolation",defineClass(x3dom.nodeTypes.X3DInterpolatorNode,function(ctx){x3dom.nodeTypes.CoordinateInterpolator.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'keyValue',[]);if(ctx&&ctx.xmlNode.hasAttribute('keyValue')){this._vf.keyValue=[];var arr=x3dom.fields.MFVec3f.parse(ctx.xmlNode.getAttribute('keyValue'));var key=this._vf.key.length>0?this._vf.key.length:1;var len=arr.length/key;for(var i=0;i<key;i++){var val=new x3dom.fields.MFVec3f();for(var j=0;j<len;j++){val.push(arr[i*len+j]);}
this._vf.keyValue.push(val);}}},{fieldChanged:function(fieldName)
{if(fieldName==="set_fraction")
{var value=this.linearInterp(this._vf.set_fraction,function(a,b,t){var val=new x3dom.fields.MFVec3f();for(var i=0;i<a.length;i++)
val.push(a[i].multiply(1.0-t).add(b[i].multiply(t)));return val;});this.postMessage('value_changed',value);}}}));x3dom.registerNodeType("TimeSensor","Time",defineClass(x3dom.nodeTypes.X3DSensorNode,function(ctx){x3dom.nodeTypes.TimeSensor.superClass.call(this,ctx);if(ctx)
ctx.doc._nodeBag.timer.push(this);else
x3dom.debug.logWarning("TimeSensor: No runtime context found!");this.addField_SFTime(ctx,'cycleInterval',1);this.addField_SFBool(ctx,'enabled',true);this.addField_SFBool(ctx,'loop',false);this.addField_SFTime(ctx,'startTime',0);this.addField_SFTime(ctx,'stopTime',0);this.addField_SFTime(ctx,'pauseTime',0);this.addField_SFTime(ctx,'resumeTime',0);this.addField_SFTime(ctx,'cycleTime',0);this.addField_SFTime(ctx,'elapsedTime',0);this.addField_SFFloat(ctx,'fraction_changed',0);this.addField_SFBool(ctx,'isActive',false);this.addField_SFBool(ctx,'isPaused',false);this.addField_SFTime(ctx,'time',0);this.addField_SFBool(ctx,'first',true);this.addField_SFFloat(ctx,'firstCycle',0.0);this._prevCycle=-1;this._lastTime=0;this._cycleStopTime=0;this._activatedTime=0;if(this._vf.startTime>0){this._updateCycleStopTime();}
this._backupStartTime=this._vf.startTime;this._backupStopTime=this._vf.stopTime;this._backupCycleInterval=this._vf.cycleInterval;},{tick:function(time)
{if(!this._vf.enabled){this._lastTime=time;return false;}
var isActive=(this._vf.cycleInterval>0&&time>=this._vf.startTime&&(time<this._vf.stopTime||this._vf.stopTime<=this._vf.startTime)&&(this._vf.loop==true||(this._vf.loop==false&&time<this._cycleStopTime)));if(isActive&&!this._vf.isActive){this.postMessage('isActive',true);this._activatedTime=time;}
if(isActive||this._vf.isActive){this.postMessage('elapsedTime',time-this._activatedTime);var isPaused=(time>=this._vf.pauseTime&&this._vf.pauseTime>this._vf.resumeTime);if(isPaused&&!this._vf.isPaused){this.postMessage('isPaused',true);this.postMessage('pauseTime',time);}else if(!isPaused&&this._vf.isPaused){this.postMessage('isPaused',false);this.postMessage('resumeTime',time);}
if(!isPaused){var cycleFrac=this._getCycleAt(time);var cycle=Math.floor(cycleFrac);var cycleTime=this._vf.startTime+cycle*this._vf.cycleInterval;var adjustTime=0;if(this._vf.stopTime>this._vf.startTime&&this._lastTime<this._vf.stopTime&&time>=this._vf.stopTime)
adjustTime=this._vf.stopTime;else if(this._lastTime<cycleTime&&time>=cycleTime)
adjustTime=cycleTime;if(adjustTime>0){time=adjustTime;cycleFrac=this._getCycleAt(time);cycle=Math.floor(cycleFrac);}
var fraction=cycleFrac-cycle;if(fraction<x3dom.fields.Eps){fraction=(this._lastTime<this._vf.startTime?0.0:1.0);this.postMessage('cycleTime',time);}
this.postMessage('fraction_changed',fraction);this.postMessage('time',time);}}
if(!isActive&&this._vf.isActive)
this.postMessage('isActive',false);this._lastTime=time;return true;},fieldChanged:function(fieldName)
{if(fieldName=="enabled"){if(!this._vf.enabled&&this._vf.isActive){this.postMessage('isActive',false);}}
else if(fieldName=="startTime"){if(this._vf.isActive){this._vf.startTime=this._backupStartTime;return;}
this._backupStartTime=this._vf.startTime;this._updateCycleStopTime();}
else if(fieldName=="stopTime"){if(this._vf.isActive&&this._vf.stopTime<=this._vf.startTime){this._vf.stopTime=this._backupStopTime;return;}
this._backupStopTime=this._vf.stopTime;}
else if(fieldName=="cycleInterval"){if(this._vf.isActive){this._vf.cycleInterval=this._backupCycleInterval;return;}
this._backupCycleInterval=this._vf.cycleInterval;this._updateCycleStopTime();}
else if(fieldName=="loop"){this._updateCycleStopTime();}},parentRemoved:function(parent)
{if(this._parentNodes.length===0){var doc=this.findX3DDoc();for(var i=0,n=doc._nodeBag.timer.length;i<n;i++){if(doc._nodeBag.timer[i]===this){doc._nodeBag.timer.splice(i,1);}}}},_getCycleAt:function(time)
{return Math.max(0.0,time-this._vf.startTime)/this._vf.cycleInterval;},_updateCycleStopTime:function()
{if(this._vf.loop==false){var now=new Date().getTime()/1000;var cycleToStop=Math.floor(this._getCycleAt(now))+1;this._cycleStopTime=this._vf.startTime+cycleToStop*this._vf.cycleInterval;}
else{this._cycleStopTime=0;}}}));x3dom.registerNodeType("X3DTimeDependentNode","Time",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DTimeDependentNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'loop',false);}));x3dom.registerNodeType("Anchor","Networking",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Anchor.superClass.call(this,ctx);this.addField_MFString(ctx,'url',[]);this.addField_MFString(ctx,'parameter',[]);},{doIntersect:function(line){var isect=false;for(var i=0;i<this._childNodes.length;i++){if(this._childNodes[i]){isect=this._childNodes[i].doIntersect(line)||isect;}}
return isect;},handleTouch:function(){var url=this._vf.url.length?this._vf.url[0]:"";var aPos=url.search("#");var anchor="";if(aPos>=0)
anchor=url.slice(aPos+1);var param=this._vf.parameter.length?this._vf.parameter[0]:"";var tPos=param.search("target=");var target="";if(tPos>=0)
target=param.slice(tPos+7);x3dom.debug.logInfo("Anchor url="+url+", target="+target+", #viewpoint="+anchor);if(target.length==0||target=="_blank"){window.open(this._nameSpace.getURL(url),target);}
else{window.location=this._nameSpace.getURL(url);}}}));x3dom.registerNodeType("Inline","Networking",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Inline.superClass.call(this,ctx);this.addField_MFString(ctx,'url',[]);this.addField_SFBool(ctx,'load',true);this.addField_MFString(ctx,'nameSpaceName',[]);this.addField_SFBool(ctx,'mapDEFToID',false);this.count=0;},{fieldChanged:function(fieldName)
{if(fieldName=="url"){if(this._vf.nameSpaceName.length!=0){var node=this._xmlNode;if(node.hasChildNodes())
{while(node.childNodes.length>=1)
{node.removeChild(node.firstChild);}}}
var xhr=this.nodeChanged();xhr=null;}},fireEvents:function(eventType)
{if(this._xmlNode&&(this._xmlNode['on'+eventType]||this._xmlNode.hasAttribute('on'+eventType)||this._listeners[eventType]))
{var event={target:this._xmlNode,type:eventType,error:(eventType=="error")?"XMLHttpRequest Error":"",cancelBubble:false,stopPropagation:function(){this.cancelBubble=true;}};try{var attrib=this._xmlNode["on"+eventType];if(typeof(attrib)==="function"){attrib.call(this._xmlNode,event);}
else{var funcStr=this._xmlNode.getAttribute("on"+eventType);var func=new Function('event',funcStr);func.call(this._xmlNode,event);}
var list=this._listeners[eventType];if(list){for(var i=0;i<list.length;i++){list[i].call(this._xmlNode,event);}}}
catch(ex){x3dom.debug.logException(ex);}}},nodeChanged:function()
{var that=this;var xhr=new window.XMLHttpRequest();if(xhr.overrideMimeType)
xhr.overrideMimeType('text/xml');this._nameSpace.doc.downloadCount+=1;xhr.onreadystatechange=function()
{if(xhr.readyState!=4){return xhr;}
if(xhr.status===202&&that.count<10){that.count++;x3dom.debug.logInfo('Statuscode 202 and send new Request');window.setTimeout(function(){that.nodeChanged();},5000);return xhr;}
else if((xhr.status!==200)&&(xhr.status!==0)){that.fireEvents("error");x3dom.debug.logError('XMLHttpRequest requires a web server running!');that._nameSpace.doc.downloadCount-=1;that.count=0;return xhr;}
else if((xhr.status==200)||(xhr.status==0)){that.count=0;}
x3dom.debug.logInfo('Inline: downloading '+that._vf.url[0]+' done.');var inlScene=null,newScene=null,nameSpace=null,xml=null;if(navigator.appName!="Microsoft Internet Explorer")
xml=xhr.responseXML;else
xml=new DOMParser().parseFromString(xhr.responseText,"text/xml");if(xml!==undefined&&xml!==null)
{inlScene=xml.getElementsByTagName('Scene')[0]||xml.getElementsByTagName('scene')[0];}
else{that.fireEvents("error");}
if(inlScene)
{nameSpace=new x3dom.NodeNameSpace("",that._nameSpace.doc);var url=that._vf.url.length?that._vf.url[0]:"";if((url[0]==='/')||(url.indexOf(":")>=0))
nameSpace.setBaseURL(url);else
nameSpace.setBaseURL(that._nameSpace.baseURL+url);newScene=nameSpace.setupTree(inlScene);if(that._vf.nameSpaceName.length!=0)
{Array.forEach(inlScene.childNodes,function(childDomNode)
{if(childDomNode instanceof Element)
{setNamespace(that._vf.nameSpaceName,childDomNode,that._vf.mapDEFToID);that._xmlNode.appendChild(childDomNode);}});}}
else{if(xml&&xml.localName)
x3dom.debug.logError('No Scene in '+xml.localName);else
x3dom.debug.logError('No Scene in resource');}
var global=x3dom.getGlobal();while(that._childNodes.length!==0)
global['_remover']=that.removeChild(that._childNodes[0]);delete global['_remover'];if(newScene)
{that.addChild(newScene);that._nameSpace.doc.downloadCount-=1;that._nameSpace.doc.needRender=true;x3dom.debug.logInfo('Inline: added '+that._vf.url[0]+' to scene.');if(that._nameSpace.doc._scene)
that._nameSpace.doc._scene.updateVolume();window.setTimeout(function(){that._nameSpace.doc._scene.updateVolume();that._nameSpace.doc.needRender=true;},1000);that.fireEvents("load");}
newScene=null;nameSpace=null;inlScene=null;xml=null;return xhr;};if(this._vf.url.length&&this._vf.url[0].length)
{xhr.open('GET',encodeURI(this._nameSpace.getURL(this._vf.url[0])),true);try{xhr.send(null);}
catch(ex){this.fireEvents("error");x3dom.debug.logError(this._vf.url[0]+": "+ex);}}
return xhr;}}));function setNamespace(prefix,childDomNode,mapDEFToID)
{if(childDomNode instanceof Element&&childDomNode.__setAttribute!==undefined){if(childDomNode.hasAttribute('id')){childDomNode.__setAttribute('id',prefix.toString().replace(' ','')+'__'+childDomNode.getAttribute('id'));}else if(childDomNode.hasAttribute('DEF')&&mapDEFToID){childDomNode.__setAttribute('id',prefix.toString().replace(' ','')+'__'+childDomNode.getAttribute('DEF'));}}
if(childDomNode.hasChildNodes()){Array.forEach(childDomNode.childNodes,function(children){setNamespace(prefix,children,mapDEFToID);});}}
x3dom.registerNodeType("X3DBackgroundNode","EnvironmentalEffects",defineClass(x3dom.nodeTypes.X3DBindableNode,function(ctx){x3dom.nodeTypes.X3DBackgroundNode.superClass.call(this,ctx);this.addField_SFBool(ctx,'withCredentials',false);this._dirty=true;},{getSkyColor:function(){return new x3dom.fields.SFColor(0,0,0);},getTransparency:function(){return 0;},getTexUrl:function(){return[];}}));x3dom.registerNodeType("X3DFogNode","EnvironmentalEffects",defineClass(x3dom.nodeTypes.X3DBindableNode,function(ctx){x3dom.nodeTypes.X3DFogNode.superClass.call(this,ctx);},{}));x3dom.registerNodeType("Fog","EnvironmentalEffects",defineClass(x3dom.nodeTypes.X3DFogNode,function(ctx){x3dom.nodeTypes.Fog.superClass.call(this,ctx);this.addField_SFColor(ctx,'color',1,1,1);this.addField_SFString(ctx,'fogType',"LINEAR");this.addField_SFFloat(ctx,'visibilityRange',0);},{}));x3dom.registerNodeType("Background","EnvironmentalEffects",defineClass(x3dom.nodeTypes.X3DBackgroundNode,function(ctx){x3dom.nodeTypes.Background.superClass.call(this,ctx);var trans=(ctx&&ctx.autoGen)?1:0;this.addField_MFColor(ctx,'skyColor',[new x3dom.fields.SFColor(0,0,0)]);this.addField_MFFloat(ctx,'skyAngle',[]);this.addField_MFColor(ctx,'groundColor',[]);this.addField_MFFloat(ctx,'groundAngle',[]);this.addField_SFFloat(ctx,'transparency',trans);this.addField_MFString(ctx,'backUrl',[]);this.addField_MFString(ctx,'bottomUrl',[]);this.addField_MFString(ctx,'frontUrl',[]);this.addField_MFString(ctx,'leftUrl',[]);this.addField_MFString(ctx,'rightUrl',[]);this.addField_MFString(ctx,'topUrl',[]);},{fieldChanged:function(fieldName)
{if(fieldName.indexOf("Url")>0||fieldName.search("sky")>=0||fieldName.search("ground")>=0){this._dirty=true;}
else if(fieldName.indexOf("bind")>=0){this.bind(this._vf.bind);}},getSkyColor:function(){return this._vf.skyColor;},getGroundColor:function(){return this._vf.groundColor;},getTransparency:function(){return this._vf.transparency;},getTexUrl:function(){return[this._nameSpace.getURL(this._vf.backUrl[0]),this._nameSpace.getURL(this._vf.frontUrl[0]),this._nameSpace.getURL(this._vf.bottomUrl[0]),this._nameSpace.getURL(this._vf.topUrl[0]),this._nameSpace.getURL(this._vf.leftUrl[0]),this._nameSpace.getURL(this._vf.rightUrl[0])];}}));x3dom.registerNodeType("X3DViewpointNode","Navigation",defineClass(x3dom.nodeTypes.X3DBindableNode,function(ctx){x3dom.nodeTypes.X3DViewpointNode.superClass.call(this,ctx);},{}));x3dom.registerNodeType("X3DNavigationInfoNode","Navigation",defineClass(x3dom.nodeTypes.X3DBindableNode,function(ctx){x3dom.nodeTypes.X3DNavigationInfoNode.superClass.call(this,ctx);},{}));x3dom.registerNodeType("Viewpoint","Navigation",defineClass(x3dom.nodeTypes.X3DViewpointNode,function(ctx){x3dom.nodeTypes.Viewpoint.superClass.call(this,ctx);this.addField_SFFloat(ctx,'fieldOfView',0.785398);this.addField_SFVec3f(ctx,'position',0,0,10);this.addField_SFRotation(ctx,'orientation',0,0,0,1);this.addField_SFVec3f(ctx,'centerOfRotation',0,0,0);this.addField_SFFloat(ctx,'zNear',-1);this.addField_SFFloat(ctx,'zFar',-1);this._viewMatrix=x3dom.fields.SFMatrix4f.translation(this._vf.position).mult(this._vf.orientation.toMatrix()).inverse();this._projMatrix=null;this._lastAspect=1.0;this._zRatio=10000;this._zNear=this._vf.zNear;this._zFar=this._vf.zFar;this._imgPlaneHeightAtDistOne=2.0*Math.tan(this._vf.fieldOfView/2.0);},{fieldChanged:function(fieldName){if(fieldName=="position"||fieldName=="orientation"){this.resetView();}
else if(fieldName=="fieldOfView"||fieldName=="zNear"||fieldName=="zFar"){this._projMatrix=null;this._zNear=this._vf.zNear;this._zFar=this._vf.zFar;this._imgPlaneHeightAtDistOne=2.0*Math.tan(this._vf.fieldOfView/2.0);}
else if(fieldName.indexOf("bind")>=0){this.bind(this._vf.bind);}},activate:function(prev){if(prev){this._nameSpace.doc._viewarea.animateTo(this,prev._autoGen?null:prev);}
x3dom.nodeTypes.X3DViewpointNode.prototype.activate.call(this,prev);this._nameSpace.doc._viewarea._needNavigationMatrixUpdate=true;},deactivate:function(prev){x3dom.nodeTypes.X3DViewpointNode.prototype.deactivate.call(this,prev);},getCenterOfRotation:function(){return this._vf.centerOfRotation;},getViewMatrix:function(){return this._viewMatrix;},getFieldOfView:function(){return this._vf.fieldOfView;},setView:function(newView){var mat=this.getCurrentTransform();mat=mat.inverse();this._viewMatrix=mat.mult(newView);},resetView:function(){this._viewMatrix=x3dom.fields.SFMatrix4f.translation(this._vf.position).mult(this._vf.orientation.toMatrix()).inverse();},getTransformation:function(){return this.getCurrentTransform();},getNear:function(){return this._zNear;},getFar:function(){return this._zFar;},getImgPlaneHeightAtDistOne:function(){return this._imgPlaneHeightAtDistOne;},getProjectionMatrix:function(aspect)
{var fovy=this._vf.fieldOfView;var zfar=this._vf.zFar;var znear=this._vf.zNear;if(znear<=0||zfar<=0)
{var nearScale=0.8,farScale=1.2;var viewarea=this._nameSpace.doc._viewarea;var min=new x3dom.fields.SFVec3f();min.setValues(viewarea._scene._lastMin);var max=new x3dom.fields.SFVec3f();max.setValues(viewarea._scene._lastMax);var dia=max.subtract(min);var sRad=dia.length()/2;var mat=viewarea.getViewMatrix().inverse();var vp=mat.e3();var sCenter=min.add(dia.multiply(0.5));var vDist=(vp.subtract(sCenter)).length();if(sRad){if(vDist>sRad)
znear=(vDist-sRad)*nearScale;else
znear=0;zfar=(vDist+sRad)*farScale;}
else{znear=0.1;zfar=100000;}
var zNearLimit=zfar/this._zRatio;znear=Math.max(znear,Math.max(x3dom.fields.Eps,zNearLimit));if(this._vf.zFar>0)
zfar=this._vf.zFar;if(this._vf.zNear>0)
znear=this._vf.zNear;var div=znear-zfar;if(this._projMatrix!=null&&div!=0)
{this._projMatrix._22=(znear+zfar)/div;this._projMatrix._23=2*znear*zfar/div;}}
this._zNear=znear;this._zFar=zfar;if(this._projMatrix==null)
{var f=1/Math.tan(fovy/2);this._projMatrix=new x3dom.fields.SFMatrix4f(f/aspect,0,0,0,0,f,0,0,0,0,(znear+zfar)/(znear-zfar),2*znear*zfar/(znear-zfar),0,0,-1,0);this._lastAspect=aspect;}
else if(this._lastAspect!==aspect)
{this._projMatrix._00=(1/Math.tan(fovy/2))/aspect;this._lastAspect=aspect;}
return this._projMatrix;}}));x3dom.registerNodeType("OrthoViewpoint","Navigation",defineClass(x3dom.nodeTypes.X3DViewpointNode,function(ctx){x3dom.nodeTypes.OrthoViewpoint.superClass.call(this,ctx);this.addField_MFFloat(ctx,'fieldOfView',[-1,-1,1,1]);this.addField_SFVec3f(ctx,'position',0,0,10);this.addField_SFRotation(ctx,'orientation',0,0,0,1);this.addField_SFVec3f(ctx,'centerOfRotation',0,0,0);this.addField_SFFloat(ctx,'zNear',0.1);this.addField_SFFloat(ctx,'zFar',10000);this._viewMatrix=null;this._projMatrix=null;this._lastAspect=1.0;this.resetView();},{fieldChanged:function(fieldName){if(fieldName=="position"||fieldName=="orientation"){this.resetView();}
else if(fieldName=="fieldOfView"||fieldName=="zNear"||fieldName=="zFar"){this._projMatrix=null;this.resetView();}
else if(fieldName.indexOf("bind")>=0){this.bind(this._vf.bind);}},activate:function(prev){if(prev){this._nameSpace.doc._viewarea.animateTo(this,prev);}
x3dom.nodeTypes.X3DViewpointNode.prototype.activate.call(this,prev);this._nameSpace.doc._viewarea._needNavigationMatrixUpdate=true;},deactivate:function(prev){x3dom.nodeTypes.X3DViewpointNode.prototype.deactivate.call(this,prev);},getCenterOfRotation:function(){return this._vf.centerOfRotation;},getViewMatrix:function(){return this._viewMatrix;},getFieldOfView:function(){return 1.57079633;},setView:function(newView){var mat=this.getCurrentTransform();mat=mat.inverse();this._viewMatrix=mat.mult(newView);},resetView:function(){var offset=x3dom.fields.SFMatrix4f.translation(new x3dom.fields.SFVec3f((this._vf.fieldOfView[0]+this._vf.fieldOfView[2])/2,(this._vf.fieldOfView[1]+this._vf.fieldOfView[3])/2,0));this._viewMatrix=x3dom.fields.SFMatrix4f.translation(this._vf.position).mult(this._vf.orientation.toMatrix());this._viewMatrix=this._viewMatrix.mult(offset).inverse();},getTransformation:function(){return this.getCurrentTransform();},getNear:function(){return this._vf.zNear;},getFar:function(){return this._vf.zFar;},getProjectionMatrix:function(aspect)
{if(this._projMatrix==null)
{var near=this.getNear();var far=this.getFar();var left=this._vf.fieldOfView[0];var bottom=this._vf.fieldOfView[1];var right=this._vf.fieldOfView[2];var top=this._vf.fieldOfView[3];var rl=(right-left)/2;var tb=(top-bottom)/2;var fn=far-near;if(aspect<(rl/tb))
tb=rl/aspect;else
rl=tb*aspect;left=-rl;right=rl;bottom=-tb;top=tb;rl*=2;tb*=2;this._projMatrix=new x3dom.fields.SFMatrix4f(2/rl,0,0,-(right+left)/rl,0,2/tb,0,-(top+bottom)/tb,0,0,-2/fn,-(far+near)/fn,0,0,0,1);}
this._lastAspect=aspect;return this._projMatrix;}}));x3dom.registerNodeType("Viewfrustum","Navigation",defineClass(x3dom.nodeTypes.X3DViewpointNode,function(ctx){x3dom.nodeTypes.Viewfrustum.superClass.call(this,ctx);this.addField_SFMatrix4f(ctx,'modelview',1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);this.addField_SFMatrix4f(ctx,'projection',1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);this._viewMatrix=this._vf.modelview.inverse();this._projMatrix=this._vf.projection;},{fieldChanged:function(fieldName){if(fieldName=="modelview"){this._viewMatrix=this._vf.modelview.inverse();}
else if(fieldName=="projection"){this._projMatrix=this._vf.projection;}
else if(fieldName.indexOf("bind")>=0){this.bind(this._vf.bind);}},activate:function(prev){if(prev){this._nameSpace.doc._viewarea.animateTo(this,prev);}
x3dom.nodeTypes.X3DViewpointNode.prototype.activate.call(this,prev);this._nameSpace.doc._viewarea._needNavigationMatrixUpdate=true;},deactivate:function(prev){x3dom.nodeTypes.X3DViewpointNode.prototype.deactivate.call(this,prev);},getCenterOfRotation:function(){return new x3dom.fields.SFVec3f(0,0,0);},getViewMatrix:function(){return this._viewMatrix;},getFieldOfView:function(){return(2.0*Math.atan(1.0/this._projMatrix._11));},setView:function(newView){var mat=this.getCurrentTransform();mat=mat.inverse();this._viewMatrix=mat.mult(newView);},resetView:function(){this._viewMatrix=this._vf.modelview.inverse();},getTransformation:function(){return this.getCurrentTransform();},getProjectionMatrix:function(aspect){return this._projMatrix;}}));x3dom.registerNodeType("NavigationInfo","Navigation",defineClass(x3dom.nodeTypes.X3DNavigationInfoNode,function(ctx){x3dom.nodeTypes.NavigationInfo.superClass.call(this,ctx);this.addField_SFBool(ctx,'headlight',true);this.addField_MFString(ctx,'type',["EXAMINE","ANY"]);this.addField_MFFloat(ctx,'typeParams',[-0.4,60]);this.addField_MFFloat(ctx,'avatarSize',[0.25,1.6,0.75]);this.addField_SFFloat(ctx,'speed',1.0);this.addField_SFFloat(ctx,'visibilityLimit',0.0);this.addField_SFTime(ctx,'transitionTime',1.0);this.addField_MFString(ctx,'transitionType',["LINEAR"]);x3dom.debug.logInfo("NavType: "+this._vf.type[0].toLowerCase());this._heliUpdated=false;},{fieldChanged:function(fieldName){if(fieldName=="typeParams"){this._heliUpdated=false;}
else if(fieldName=="type"){var type=this._vf.type[0].toLowerCase();switch(type){case'game':this._nameSpace.doc._viewarea.initMouseState();break;case'helicopter':this._heliUpdated=false;break;default:break;}
x3dom.debug.logInfo("Switch to "+type+" mode.");}},getType:function(){return this._vf.type[0].toLowerCase();},getTypeParams:function(){var theta=(this._vf.typeParams.length>=1)?this._vf.typeParams[0]:0;var height=(this._vf.typeParams.length>=2)?this._vf.typeParams[1]:0;return[theta,height];},setTypeParams:function(params){for(var i=0;i<params.length;i++){this._vf.typeParams[i]=params[i];}},setType:function(type,viewarea){var navType=type.toLowerCase();switch(navType){case'game':if(this._vf.type[0].toLowerCase()!==navType){if(viewarea)
viewarea.initMouseState();else
this._nameSpace.doc._viewarea.initMouseState();}
break;case'helicopter':if(this._vf.type[0].toLowerCase()!==navType){this._heliUpdated=false;}
break;default:break;}
this._vf.type[0]=navType;x3dom.debug.logInfo("Switch to "+navType+" mode.");}}));x3dom.registerNodeType("Billboard","Navigation",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Billboard.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'axisOfRotation',0,1,0);this._eye=new x3dom.fields.SFVec3f(0,0,0);this._eyeViewUp=new x3dom.fields.SFVec3f(0,0,0);this._eyeLook=new x3dom.fields.SFVec3f(0,0,0);},{collectDrawableObjects:function(transform,out)
{if(!this._vf.render||!out){return;}
out.cnt++;var min=x3dom.fields.SFVec3f.MAX();var max=x3dom.fields.SFVec3f.MIN();var ok=this.getVolume(min,max);var rotMat=x3dom.fields.SFMatrix4f.identity();var mid=max.add(min).multiply(0.5);var billboard_to_viewer=this._eye.subtract(mid);if(this._vf.axisOfRotation.equals(new x3dom.fields.SFVec3f(0,0,0),x3dom.fields.Eps)){var rot1=x3dom.fields.Quaternion.rotateFromTo(billboard_to_viewer,new x3dom.fields.SFVec3f(0,0,1));rotMat=rot1.toMatrix().transpose();var yAxis=rotMat.multMatrixPnt(new x3dom.fields.SFVec3f(0,1,0)).normalize();var zAxis=rotMat.multMatrixPnt(new x3dom.fields.SFVec3f(0,0,1)).normalize();if(!this._eyeViewUp.equals(new x3dom.fields.SFVec3f(0,0,0),x3dom.fields.Eps)){var rot2=x3dom.fields.Quaternion.rotateFromTo(this._eyeLook,zAxis);var rotatedyAxis=rot2.toMatrix().transpose().multMatrixVec(yAxis);var rot3=x3dom.fields.Quaternion.rotateFromTo(this._eyeViewUp,rotatedyAxis);rotMat=rot2.toMatrix().transpose().mult(rotMat);rotMat=rot3.toMatrix().transpose().mult(rotMat);}}
else{var normalPlane=this._vf.axisOfRotation.cross(billboard_to_viewer).normalize();if(this._eye.z<0){normalPlane=normalPlane.multiply(-1);}
var degreesToRotate=Math.asin(normalPlane.dot(new x3dom.fields.SFVec3f(0,0,1)));if(this._eye.z<0){degreesToRotate+=Math.PI;}
rotMat=x3dom.fields.SFMatrix4f.parseRotation(this._vf.axisOfRotation.x+", "+this._vf.axisOfRotation.y+", "+
this._vf.axisOfRotation.z+", "+degreesToRotate*(-1));}
for(var i=0,i_n=this._childNodes.length;i<i_n;i++)
{var cnode=this._childNodes[i];if(cnode){var childTransform=cnode.transformMatrix(transform.mult(rotMat));cnode.collectDrawableObjects(childTransform,out);}}
if(out!==null){out.Billboards.push([transform,this]);}}}));x3dom.registerNodeType("Collision","Navigation",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.Collision.superClass.call(this,ctx);this.addField_SFBool(ctx,"enabled",true);this.addField_SFNode("proxy",x3dom.nodeTypes.X3DGroupingNode);},{collectDrawableObjects:function(transform,out)
{if(!this._vf.render||!out){return;}
out.cnt++;for(var i=0,i_n=this._childNodes.length;i<i_n;i++)
{var cnode=this._childNodes[i];if(cnode&&(cnode!==this._cf.proxy.node))
{var childTransform=cnode.transformMatrix(transform);cnode.collectDrawableObjects(childTransform,out);}}}}));x3dom.registerNodeType("X3DLODNode","Navigation",defineClass(x3dom.nodeTypes.X3DGroupingNode,function(ctx){x3dom.nodeTypes.X3DLODNode.superClass.call(this,ctx);this.addField_SFBool(ctx,"forceTransitions",false);this.addField_SFVec3f(ctx,"center",0,0,0);this._eye=new x3dom.fields.SFVec3f(0,0,0);},{collectDrawableObjects:function(transform,out)
{if(!this._vf.render||!out){return;}
out.cnt++;this.visitChildren(transform,out);out.LODs.push([transform,this]);},visitChildren:function(transform,out){}}));x3dom.registerNodeType("LOD","Navigation",defineClass(x3dom.nodeTypes.X3DLODNode,function(ctx){x3dom.nodeTypes.LOD.superClass.call(this,ctx);this.addField_MFFloat(ctx,"range",[]);this._needReRender=true;},{visitChildren:function(transform,out)
{var i=0,n=this._childNodes.length;var min=x3dom.fields.SFVec3f.MAX();var max=x3dom.fields.SFVec3f.MIN();var ok=this.getVolume(min,max);var mid=max.add(min).multiply(0.5).add(this._vf.center);var len=mid.subtract(this._eye).length();while(i<this._vf.range.length&&len>this._vf.range[i]){i++;}
if(i&&i>=n){i=n-1;}
var cnode=this._childNodes[i];if(n&&cnode)
{var childTransform=cnode.transformMatrix(transform);cnode.collectDrawableObjects(childTransform,out);}
if(this._needReRender){this._needReRender=false;this._nameSpace.doc.needRender=true;}},nodeChanged:function(){this._needReRender=true;},fieldChanged:function(fieldName){this._needReRender=true;}}));x3dom.registerNodeType("DynamicLOD","Navigation",defineClass(x3dom.nodeTypes.X3DLODNode,function(ctx){x3dom.nodeTypes.DynamicLOD.superClass.call(this,ctx);this.addField_SFFloat(ctx,'subScale',0.5);this.addField_SFVec2f(ctx,'size',2,2);this.addField_SFVec2f(ctx,'subdivision',1,1);this.addField_SFNode('root',x3dom.nodeTypes.X3DShapeNode);this.addField_SFString(ctx,'urlHead',"http://r");this.addField_SFString(ctx,'urlCenter',".ortho.tiles.virtualearth.net/tiles/h");this.addField_SFString(ctx,'urlTail',".png?g=-1");this.rootGeometry=new x3dom.nodeTypes.Plane(ctx);this.level=0;this.quadrant=4;this.cell="";},{nodeChanged:function()
{var root=this._cf.root.node;if(root==null||root._cf.geometry.node!=null)
return;this.rootGeometry._vf.size.setValues(this._vf.size);this.rootGeometry._vf.subdivision.setValues(this._vf.subdivision);this.rootGeometry._vf.center.setValues(this._vf.center);this.rootGeometry.fieldChanged("subdivision");this._cf.root.node.addChild(this.rootGeometry);this.rootGeometry.nodeChanged();this._cf.root.node.nodeChanged();this._nameSpace.doc.needRender=true;},visitChildren:function(transform,out)
{var root=this._cf.root.node;if(root==null)
return;var l,len=this._vf.center.subtract(this._eye).length();if(len>x3dom.fields.Eps&&len*this._vf.subScale<=this._vf.size.length()){if(this._childNodes.length<=1){var offset=new Array(new x3dom.fields.SFVec3f(-0.25*this._vf.size.x,0.25*this._vf.size.y,0),new x3dom.fields.SFVec3f(0.25*this._vf.size.x,0.25*this._vf.size.y,0),new x3dom.fields.SFVec3f(-0.25*this._vf.size.x,-0.25*this._vf.size.y,0),new x3dom.fields.SFVec3f(0.25*this._vf.size.x,-0.25*this._vf.size.y,0));for(l=0;l<4;l++){var node=new x3dom.nodeTypes.DynamicLOD();node._nameSpace=this._nameSpace;node._eye.setValues(this._eye);node.level=this.level+1;node.quadrant=l;node.cell=this.cell+l;node._vf.urlHead=this._vf.urlHead;node._vf.urlCenter=this._vf.urlCenter;node._vf.urlTail=this._vf.urlTail;node._vf.center=this._vf.center.add(offset[l]);node._vf.size=this._vf.size.multiply(0.5);node._vf.subdivision.setValues(this._vf.subdivision);var app=new x3dom.nodeTypes.Appearance();var tex=new x3dom.nodeTypes.ImageTexture();tex._nameSpace=this._nameSpace;tex._vf.url[0]=this._vf.urlHead+node.quadrant+this._vf.urlCenter+node.cell+this._vf.urlTail;app.addChild(tex);tex.nodeChanged();var shape=new x3dom.nodeTypes.Shape();shape._nameSpace=this._nameSpace;shape.addChild(app);app.nodeChanged();node.addChild(shape,"root");shape.nodeChanged();this.addChild(node);node.nodeChanged();}}
else{for(l=1;l<this._childNodes.length;l++){this._childNodes[l].collectDrawableObjects(transform,out);}}}
else{root.collectDrawableObjects(transform,out);}},getVolume:function(min,max){var vol=this._graph.volume;if(!vol.isValid()){min.setValues(this._vf.center);min.x-=0.5*this._vf.size.x;min.y-=0.5*this._vf.size.y;min.z-=x3dom.fields.Eps;max.setValues(this._vf.center);max.x+=0.5*this._vf.size.x;max.y+=0.5*this._vf.size.y;max.z+=x3dom.fields.Eps;vol.setBounds(min,max);}
else{vol.getBounds(min,max);}
return true;}}));x3dom.registerNodeType("Environment","Navigation",defineClass(x3dom.nodeTypes.X3DBindableNode,function(ctx){x3dom.nodeTypes.Environment.superClass.call(this,ctx);this.addField_SFFloat(ctx,'globalShadowIntensity',0);this.addField_SFInt32(ctx,'shadowMapSize',512);this.addField_SFString(ctx,'shadowMode',"perspectiveHardShadow");this.addField_SFFloat(ctx,'shadowOffset',4);this.addField_SFFloat(ctx,'shadowSmoothness',0.5);this.addField_SFBool(ctx,'shadowExcludeTransparentObjects',true);this.addField_MFNode('shadowExcludeObjects',x3dom.nodeTypes.X3DNode);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("X3DFontStyleNode","Text",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.X3DFontStyleNode.superClass.call(this,ctx);}));x3dom.registerNodeType("FontStyle","Text",defineClass(x3dom.nodeTypes.X3DFontStyleNode,function(ctx){x3dom.nodeTypes.FontStyle.superClass.call(this,ctx);this.addField_MFString(ctx,'family',['SERIF']);this.addField_SFBool(ctx,'horizontal',true);this.addField_MFString(ctx,'justify',['BEGIN']);this.addField_SFString(ctx,'language',"");this.addField_SFBool(ctx,'leftToRight',true);this.addField_SFFloat(ctx,'size',1.0);this.addField_SFFloat(ctx,'spacing',1.0);this.addField_SFString(ctx,'style',"PLAIN");this.addField_SFBool(ctx,'topToBottom',true);},{nodeChanged:function(){},fieldChanged:function(fieldName){if(fieldName=='family'||fieldName=='horizontal'||fieldName=='justify'||fieldName=='language'||fieldName=='leftToRight'||fieldName=='size'||fieldName=='spacing'||fieldName=='style'||fieldName=='topToBottom'){Array.forEach(this._parentNodes,function(node){node.fieldChanged(fieldName);});}}}));x3dom.nodeTypes.FontStyle.defaultNode=function(){if(!x3dom.nodeTypes.FontStyle._defaultNode){x3dom.nodeTypes.FontStyle._defaultNode=new x3dom.nodeTypes.FontStyle();x3dom.nodeTypes.FontStyle._defaultNode.nodeChanged();}
return x3dom.nodeTypes.FontStyle._defaultNode;};x3dom.registerNodeType("Text","Text",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.Text.superClass.call(this,ctx);this.addField_MFString(ctx,'string',[]);this.addField_MFFloat(ctx,'length',[]);this.addField_SFFloat(ctx,'maxExtent',0.0);this.addField_SFNode('fontStyle',x3dom.nodeTypes.X3DFontStyleNode);this._mesh._normals[0]=[0,0,1,0,0,1,0,0,1,0,0,1];this._mesh._texCoords[0]=[0,0,1,0,1,1,0,1];this._mesh._colors[0]=[];this._mesh._indices[0]=[0,1,2,2,3,0];this._mesh._invalidate=true;this._mesh._numFaces=2;this._mesh._numCoords=4;},{nodeChanged:function(){if(!this._cf.fontStyle.node){this.addChild(x3dom.nodeTypes.FontStyle.defaultNode());}},fieldChanged:function(fieldName){if(fieldName=='string'||fieldName=='family'||fieldName=='horizontal'||fieldName=='justify'||fieldName=='language'||fieldName=='leftToRight'||fieldName=='size'||fieldName=='spacing'||fieldName=='style'||fieldName=='topToBottom'){Array.forEach(this._parentNodes,function(node){node._dirty.texture=true;node._dirty.positions=true;});}}}));x3dom.registerNodeType("X3DSoundNode","Sound",defineClass(x3dom.nodeTypes.X3DChildNode,function(ctx){x3dom.nodeTypes.X3DSoundNode.superClass.call(this,ctx);}));x3dom.registerNodeType("Sound","Sound",defineClass(x3dom.nodeTypes.X3DSoundNode,function(ctx){x3dom.nodeTypes.Sound.superClass.call(this,ctx);this.addField_SFNode('source',x3dom.nodeTypes.X3DSoundSourceNode);},{nodeChanged:function()
{if(this._cf.source.node||!this._xmlNode){return;}
x3dom.debug.logInfo("No AudioClip child node given, searching for &lt;audio&gt; elements...");var that=this;try{Array.forEach(this._xmlNode.childNodes,function(childDomNode){if(childDomNode.nodeType===1)
{x3dom.debug.logInfo("### Found &lt;"+childDomNode.nodeName+"&gt; tag.");if(childDomNode.localName.toLowerCase()==="audio")
{var loop=childDomNode.getAttribute("loop");loop=loop?(loop.toLowerCase()==="loop"):false;var newNode=childDomNode.cloneNode(false);childDomNode.parentNode.removeChild(childDomNode);childDomNode=null;if(navigator.appName!="Microsoft Internet Explorer"){document.body.appendChild(newNode);}
var startAudio=function(){newNode.play();};var audioDone=function(){if(loop){newNode.play();}};newNode.addEventListener("canplaythrough",startAudio,true);newNode.addEventListener("ended",audioDone,true);}}});}
catch(e){}}}));x3dom.registerNodeType("X3DSoundSourceNode","Sound",defineClass(x3dom.nodeTypes.X3DTimeDependentNode,function(ctx){x3dom.nodeTypes.X3DSoundSourceNode.superClass.call(this,ctx);}));x3dom.registerNodeType("AudioClip","Sound",defineClass(x3dom.nodeTypes.X3DSoundSourceNode,function(ctx){x3dom.nodeTypes.AudioClip.superClass.call(this,ctx);this.addField_MFString(ctx,'url',[]);this.addField_SFBool(ctx,'enabled',true);this.addField_SFBool(ctx,'loop',false);this._audio=null;},{nodeChanged:function()
{this._audio=document.createElement('audio');this._audio.setAttribute('autobuffer','true');if(navigator.appName!="Microsoft Internet Explorer"){document.body.appendChild(this._audio);}
for(var i=0;i<this._vf.url.length;i++)
{var audioUrl=this._nameSpace.getURL(this._vf.url[i]);x3dom.debug.logInfo('Adding sound file: '+audioUrl);var src=document.createElement('source');src.setAttribute('src',audioUrl);this._audio.appendChild(src);}
var that=this;var startAudio=function()
{that._audio.play();};var audioDone=function()
{if(that._vf.loop===true)
{that._audio.play();}};this._audio.addEventListener("canplaythrough",startAudio,true);this._audio.addEventListener("ended",audioDone,true);},fieldChanged:function(fieldName)
{if(fieldName==="enabled")
{if(this._vf.enabled===true)
{this._audio.play();}
else
{this._audio.pause();}}
else if(fieldName==="loop")
{if(this._vf.loop===true)
{this._audio.play();}}
else if(fieldName==="url")
{this._audio.pause();while(this._audio.hasChildNodes())
{this._audio.removeChild(this._audio.firstChild);}
for(var i=0;i<this._vf.url.length;i++)
{var audioUrl=this._nameSpace.getURL(this._vf.url[i]);x3dom.debug.logInfo('Adding sound file: '+audioUrl);var src=document.createElement('source');src.setAttribute('src',audioUrl);this._audio.appendChild(src);}}}}));x3dom.registerNodeType("X3DTextureTransformNode","Texturing",defineClass(x3dom.nodeTypes.X3DAppearanceChildNode,function(ctx){x3dom.nodeTypes.X3DTextureTransformNode.superClass.call(this,ctx);}));x3dom.registerNodeType("TextureTransform","Texturing",defineClass(x3dom.nodeTypes.X3DTextureTransformNode,function(ctx){x3dom.nodeTypes.TextureTransform.superClass.call(this,ctx);this.addField_SFVec2f(ctx,'center',0,0);this.addField_SFFloat(ctx,'rotation',0);this.addField_SFVec2f(ctx,'scale',1,1);this.addField_SFVec2f(ctx,'translation',0,0);var negCenter=new x3dom.fields.SFVec3f(-this._vf.center.x,-this._vf.center.y,1);var posCenter=new x3dom.fields.SFVec3f(this._vf.center.x,this._vf.center.y,0);var trans3=new x3dom.fields.SFVec3f(this._vf.translation.x,this._vf.translation.y,0);var scale3=new x3dom.fields.SFVec3f(this._vf.scale.x,this._vf.scale.y,0);this._trafo=x3dom.fields.SFMatrix4f.translation(negCenter).mult(x3dom.fields.SFMatrix4f.scale(scale3)).mult(x3dom.fields.SFMatrix4f.rotationZ(this._vf.rotation)).mult(x3dom.fields.SFMatrix4f.translation(posCenter.add(trans3)));},{fieldChanged:function(fieldName){if(fieldName=='center'||fieldName=='rotation'||fieldName=='scale'||fieldName=='translation'){var negCenter=new x3dom.fields.SFVec3f(-this._vf.center.x,-this._vf.center.y,1);var posCenter=new x3dom.fields.SFVec3f(this._vf.center.x,this._vf.center.y,0);var trans3=new x3dom.fields.SFVec3f(this._vf.translation.x,this._vf.translation.y,0);var scale3=new x3dom.fields.SFVec3f(this._vf.scale.x,this._vf.scale.y,0);this._trafo=x3dom.fields.SFMatrix4f.translation(negCenter).mult(x3dom.fields.SFMatrix4f.scale(scale3)).mult(x3dom.fields.SFMatrix4f.rotationZ(this._vf.rotation)).mult(x3dom.fields.SFMatrix4f.translation(posCenter.add(trans3)));}},texTransformMatrix:function(){return this._trafo;}}));x3dom.registerNodeType("TextureProperties","Texturing",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.TextureProperties.superClass.call(this,ctx);this.addField_SFFloat(ctx,'anisotropicDegree',1.0);this.addField_SFColorRGBA(ctx,'borderColor',0,0,0,0);this.addField_SFInt32(ctx,'borderWidth',0);this.addField_SFString(ctx,'boundaryModeS',"REPEAT");this.addField_SFString(ctx,'boundaryModeT',"REPEAT");this.addField_SFString(ctx,'boundaryModeR',"REPEAT");this.addField_SFString(ctx,'magnificationFilter',"FASTEST");this.addField_SFString(ctx,'minificationFilter',"FASTEST");this.addField_SFString(ctx,'textureCompression',"FASTEST");this.addField_SFFloat(ctx,'texturePriority',0);this.addField_SFBool(ctx,'generateMipMaps',false);},{fieldChanged:function(fieldName)
{Array.forEach(this._parentNodes,function(texture){Array.forEach(texture._parentNodes,function(app){Array.forEach(app._parentNodes,function(shape){shape._dirty.texture=true;});});});this._nameSpace.doc.needRender=true;}}));x3dom.registerNodeType("X3DTextureNode","Texturing",defineClass(x3dom.nodeTypes.X3DAppearanceChildNode,function(ctx){x3dom.nodeTypes.X3DTextureNode.superClass.call(this,ctx);this.addField_SFInt32(ctx,'origChannelCount',0);this.addField_MFString(ctx,'url',[]);this.addField_SFBool(ctx,'repeatS',true);this.addField_SFBool(ctx,'repeatT',true);this.addField_SFNode('textureProperties',x3dom.nodeTypes.TextureProperties);this.addField_SFBool(ctx,'scale',true);this.addField_SFBool(ctx,'withCredentials',false);this._needPerFrameUpdate=false;this._isCanvas=false;this._type="diffuseMap";this._blending=(this._vf.origChannelCount==1||this._vf.origChannelCount==2);},{invalidateGLObject:function()
{Array.forEach(this._parentNodes,function(app){Array.forEach(app._parentNodes,function(shape){shape._dirty.texture=true;});});this._nameSpace.doc.needRender=true;},parentAdded:function(parent)
{Array.forEach(parent._parentNodes,function(shape){shape._dirty.texture=true;});},parentRemoved:function(parent)
{Array.forEach(parent._parentNodes,function(shape){shape._dirty.texture=true;});},nodeChanged:function()
{},fieldChanged:function(fieldName)
{if(fieldName=="url"||fieldName=="origChannelCount"||fieldName=="repeatS"||fieldName=="repeatT")
{var that=this;Array.forEach(this._parentNodes,function(app){if(x3dom.isa(app,x3dom.nodeTypes.X3DAppearanceNode)){app.nodeChanged();Array.forEach(app._parentNodes,function(shape){shape._dirty.texture=true;});}
else if(x3dom.isa(app,x3dom.nodeTypes.ImageGeometry)){var cf=null;if(that._xmlNode&&that._xmlNode.hasAttribute('containerField')){cf=that._xmlNode.getAttribute('containerField');app._dirty[cf]=true;}}});}},getTexture:function(pos){if(pos===0){return this;}
return null;},size:function(){return 1;}}));x3dom.registerNodeType("MultiTexture","Texturing",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.MultiTexture.superClass.call(this,ctx);this.addField_MFNode('texture',x3dom.nodeTypes.X3DTextureNode);},{getTexture:function(pos){if(pos>=0&&pos<this._cf.texture.nodes.length){return this._cf.texture.nodes[pos];}
return null;},getTextures:function(){return this._cf.texture.nodes;},size:function(){return this._cf.texture.nodes.length;}}));x3dom.registerNodeType("Texture","Texturing",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.Texture.superClass.call(this,ctx);this.addField_SFBool(ctx,'hideChildren',true);this._video=null;this._intervalID=0;this._canvas=null;},{nodeChanged:function()
{if(this._vf.url.length||!this._xmlNode){return;}
x3dom.debug.logInfo("No Texture URL given, searching for &lt;img&gt; elements...");var that=this;try{Array.forEach(this._xmlNode.childNodes,function(childDomNode){if(childDomNode.nodeType===1){var url=childDomNode.getAttribute("src");if(url){that._vf.url.push(url);x3dom.debug.logInfo(that._vf.url[that._vf.url.length-1]);if(childDomNode.localName==="video"){that._needPerFrameUpdate=true;that._video=document.createElement('video');that._video.setAttribute('autobuffer','true');var p=document.getElementsByTagName('body')[0];p.appendChild(that._video);that._video.style.display="none";}}
else if(childDomNode.localName.toLowerCase()==="canvas"){that._needPerFrameUpdate=true;that._isCanvas=true;that._canvas=childDomNode;}
if(that._vf.hideChildren){childDomNode.style.display="none";childDomNode.style.visibility="hidden";}
x3dom.debug.logInfo("### Found &lt;"+childDomNode.nodeName+"&gt; tag.");}});}
catch(e){}}}));x3dom.registerNodeType("RenderedTexture","Texturing",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.RenderedTexture.superClass.call(this,ctx);if(ctx)
ctx.doc._nodeBag.renderTextures.push(this);else
x3dom.debug.logWarning("RenderedTexture: No runtime context found!");this.addField_SFNode('viewpoint',x3dom.nodeTypes.X3DViewpointNode);this.addField_SFNode('background',x3dom.nodeTypes.X3DBackgroundNode);this.addField_SFNode('fog',x3dom.nodeTypes.X3DFogNode);this.addField_SFNode('scene',x3dom.nodeTypes.X3DNode);this.addField_MFNode('excludeNodes',x3dom.nodeTypes.X3DNode);this.addField_MFInt32(ctx,'dimensions',[128,128,4]);this.addField_SFString(ctx,'update','NONE');this.addField_SFBool(ctx,'showNormals',false);x3dom.debug.assert(this._vf.dimensions.length>=3);this._clearParents=true;this._needRenderUpdate=true;},{nodeChanged:function()
{this._clearParents=true;this._needRenderUpdate=true;},fieldChanged:function(fieldName)
{switch(fieldName)
{case"excludeNodes":this._clearParents=true;break;case"update":if(this._vf.update.toUpperCase()=="NEXT_FRAME_ONLY"||this._vf.update.toUpperCase()=="ALWAYS"){this._needRenderUpdate=true;}
break;default:break;}},getViewMatrix:function()
{if(this._clearParents&&this._cf.excludeNodes.nodes.length){var that=this;Array.forEach(this._cf.excludeNodes.nodes,function(node){for(var i=0,n=node._parentNodes.length;i<n;i++){if(node._parentNodes[i]===that){node._parentNodes.splice(i,1);node.parentRemoved(that);}}});this._clearParents=false;}
var vbP=this._nameSpace.doc._scene.getViewpoint();var view=this._cf.viewpoint.node;var ret_mat=null;if(view===null||view===vbP){ret_mat=this._nameSpace.doc._viewarea.getViewMatrix();}
else{var mat_viewpoint=view.getCurrentTransform();ret_mat=mat_viewpoint.mult(view.getViewMatrix());}
return ret_mat;},getProjectionMatrix:function()
{var vbP=this._nameSpace.doc._scene.getViewpoint();var view=this._cf.viewpoint.node;var ret_mat=null;if(view===null||view===vbP){ret_mat=this._nameSpace.doc._viewarea.getProjectionMatrix();}
else{var w=this._vf.dimensions[0],h=this._vf.dimensions[1];ret_mat=view.getProjectionMatrix(w/h);}
return ret_mat;},getWCtoCCMatrix:function()
{var view=this.getViewMatrix();var proj=this.getProjectionMatrix();return proj.mult(view);},parentRemoved:function(parent)
{if(this._parentNodes.length===0){var doc=this.findX3DDoc();for(var i=0,n=doc._nodeBag.renderTextures.length;i<n;i++){if(doc._nodeBag.renderTextures[i]===this){doc._nodeBag.renderTextures.splice(i,1);}}}
if(this._cf.scene.node){this._cf.scene.node.parentRemoved(this);}}}));x3dom.registerNodeType("PixelTexture","Texturing",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.PixelTexture.superClass.call(this,ctx);this.addField_SFImage(ctx,'image',0,0,0);},{fieldChanged:function(fieldName)
{if(fieldName=="image"){this.invalidateGLObject();}}}));x3dom.registerNodeType("ImageTexture","Texturing",defineClass(x3dom.nodeTypes.Texture,function(ctx){x3dom.nodeTypes.ImageTexture.superClass.call(this,ctx);},{}));x3dom.registerNodeType("MovieTexture","Texturing",defineClass(x3dom.nodeTypes.Texture,function(ctx){x3dom.nodeTypes.MovieTexture.superClass.call(this,ctx);this.addField_SFBool(ctx,'loop',false);this.addField_SFFloat(ctx,'speed',1.0);},{}));x3dom.registerNodeType("X3DEnvironmentTextureNode","CubeMapTexturing",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.X3DEnvironmentTextureNode.superClass.call(this,ctx);},{getTexUrl:function(){return[];},getTexSize:function(){return-1;}}));x3dom.registerNodeType("ComposedCubeMapTexture","CubeMapTexturing",defineClass(x3dom.nodeTypes.X3DEnvironmentTextureNode,function(ctx){x3dom.nodeTypes.ComposedCubeMapTexture.superClass.call(this,ctx);this.addField_SFNode('back',x3dom.nodeTypes.Texture);this.addField_SFNode('front',x3dom.nodeTypes.Texture);this.addField_SFNode('bottom',x3dom.nodeTypes.Texture);this.addField_SFNode('top',x3dom.nodeTypes.Texture);this.addField_SFNode('left',x3dom.nodeTypes.Texture);this.addField_SFNode('right',x3dom.nodeTypes.Texture);this._type="cubeMap";},{getTexUrl:function(){return[this._nameSpace.getURL(this._cf.back.node._vf.url[0]),this._nameSpace.getURL(this._cf.front.node._vf.url[0]),this._nameSpace.getURL(this._cf.bottom.node._vf.url[0]),this._nameSpace.getURL(this._cf.top.node._vf.url[0]),this._nameSpace.getURL(this._cf.left.node._vf.url[0]),this._nameSpace.getURL(this._cf.right.node._vf.url[0])];}}));x3dom.registerNodeType("GeneratedCubeMapTexture","CubeMapTexturing",defineClass(x3dom.nodeTypes.X3DEnvironmentTextureNode,function(ctx){x3dom.nodeTypes.GeneratedCubeMapTexture.superClass.call(this,ctx);this.addField_SFInt32(ctx,'size',128);this.addField_SFString(ctx,'update','NONE');this._type="cubeMap";x3dom.debug.logWarning("GeneratedCubeMapTexture NYI");},{getTexSize:function(){return this._vf.size;}}));x3dom.registerNodeType("X3DTextureCoordinateNode","Texturing",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.X3DTextureCoordinateNode.superClass.call(this,ctx);},{fieldChanged:function(fieldName){if(fieldName==="texCoord"||fieldName==="point"||fieldName==="parameter"||fieldName==="mode")
{Array.forEach(this._parentNodes,function(node){node.fieldChanged("texCoord");});}},parentAdded:function(parent){if(parent._mesh&&parent._cf.texCoord.node!==this){parent.fieldChanged("texCoord");}}}));x3dom.registerNodeType("TextureCoordinate","Texturing",defineClass(x3dom.nodeTypes.X3DTextureCoordinateNode,function(ctx){x3dom.nodeTypes.TextureCoordinate.superClass.call(this,ctx);this.addField_MFVec2f(ctx,'point',[]);}));x3dom.registerNodeType("TextureCoordinateGenerator","Texturing",defineClass(x3dom.nodeTypes.X3DTextureCoordinateNode,function(ctx){x3dom.nodeTypes.TextureCoordinateGenerator.superClass.call(this,ctx);this.addField_SFString(ctx,'mode',"SPHERE");this.addField_MFFloat(ctx,'parameter',[]);}));x3dom.registerNodeType("MultiTextureCoordinate","Texturing",defineClass(x3dom.nodeTypes.X3DTextureCoordinateNode,function(ctx){x3dom.nodeTypes.MultiTextureCoordinate.superClass.call(this,ctx);this.addField_MFNode('texCoord',x3dom.nodeTypes.X3DTextureCoordinateNode);}));x3dom.registerNodeType("Uniform","Shaders",defineClass(x3dom.nodeTypes.Field,function(ctx){x3dom.nodeTypes.Uniform.superClass.call(this,ctx);}));x3dom.registerNodeType("SurfaceShaderTexture","Shaders",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.SurfaceShaderTexture.superClass.call(this,ctx);this.addField_SFInt32(ctx,'textureCoordinatesId',0);this.addField_SFString(ctx,'channelMask',"DEFAULT");this.addField_SFBool(ctx,'isSRGB',false);this.addField_SFNode('texture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('textureTransform',x3dom.nodeTypes.X3DTextureTransformNode);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("X3DShaderNode","Shaders",defineClass(x3dom.nodeTypes.X3DAppearanceChildNode,function(ctx){x3dom.nodeTypes.X3DShaderNode.superClass.call(this,ctx);this.addField_SFString(ctx,'language',"");}));x3dom.registerNodeType("CommonSurfaceShader","Shaders",defineClass(x3dom.nodeTypes.X3DShaderNode,function(ctx){x3dom.nodeTypes.CommonSurfaceShader.superClass.call(this,ctx);this.addField_SFInt32(ctx,'tangentTextureCoordinatesId',-1);this.addField_SFInt32(ctx,'binormalTextureCoordinatesId',-1);this.addField_SFVec3f(ctx,'emissiveFactor',0,0,0);this.addField_SFInt32(ctx,'emissiveTextureId',-1);this.addField_SFInt32(ctx,'emissiveTextureCoordinatesId',0);this.addField_SFString(ctx,'emissiveTextureChannelMask','rgb');this.addField_SFVec3f(ctx,'ambientFactor',0.2,0.2,0.2);this.addField_SFInt32(ctx,'ambientTextureId',-1);this.addField_SFInt32(ctx,'ambientTextureCoordinatesId',0);this.addField_SFString(ctx,'ambientTextureChannelMask','rgb');this.addField_SFVec3f(ctx,'diffuseFactor',0.8,0.8,0.8);this.addField_SFInt32(ctx,'diffuseTextureId',-1);this.addField_SFInt32(ctx,'diffuseTextureCoordinatesId',0);this.addField_SFString(ctx,'diffuseTextureChannelMask','rgb');this.addField_SFVec3f(ctx,'specularFactor',0,0,0);this.addField_SFInt32(ctx,'specularTextureId',-1);this.addField_SFInt32(ctx,'specularTextureCoordinatesId',0);this.addField_SFString(ctx,'specularTextureChannelMask','rgb');this.addField_SFFloat(ctx,'shininessFactor',0.2);this.addField_SFInt32(ctx,'shininessTextureId',-1);this.addField_SFInt32(ctx,'shininessTextureCoordinatesId',0);this.addField_SFString(ctx,'shininessTextureChannelMask','a');this.addField_SFString(ctx,'normalFormat','UNORM');this.addField_SFString(ctx,'normalSpace','TANGENT');this.addField_SFInt32(ctx,'normalTextureId',-1);this.addField_SFInt32(ctx,'normalTextureCoordinatesId',0);this.addField_SFString(ctx,'normalTextureChannelMask','rgb');this.addField_SFVec3f(ctx,'reflectionFactor',0,0,0);this.addField_SFInt32(ctx,'reflectionTextureId',-1);this.addField_SFInt32(ctx,'reflectionTextureCoordinatesId',0);this.addField_SFString(ctx,'reflectionTextureChannelMask','rgb');this.addField_SFVec3f(ctx,'transmissionFactor',0,0,0);this.addField_SFInt32(ctx,'transmissionTextureId',-1);this.addField_SFInt32(ctx,'transmissionTextureCoordinatesId',0);this.addField_SFString(ctx,'transmissionTextureChannelMask','rgb');this.addField_SFVec3f(ctx,'environmentFactor',1,1,1);this.addField_SFInt32(ctx,'environmentTextureId',-1);this.addField_SFInt32(ctx,'environmentTextureCoordinatesId',0);this.addField_SFString(ctx,'environmentTextureChannelMask','rgb');this.addField_SFFloat(ctx,'relativeIndexOfRefraction',1);this.addField_SFFloat(ctx,'fresnelBlend',0);this.addField_SFNode('emissiveTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('ambientTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('diffuseTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('specularTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('shininessTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('normalTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('reflectionTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('transmissionTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('environmentTexture',x3dom.nodeTypes.X3DTextureNode);this.addField_SFVec3f(ctx,'normalScale',2,2,2);this.addField_SFVec3f(ctx,'normalBias',-1,-1,-1);this.addField_SFFloat(ctx,'alphaFactor',1);this.addField_SFBool(ctx,'invertAlphaTexture',false);this.addField_SFInt32(ctx,'alphaTextureId',-1);this.addField_SFInt32(ctx,'alphaTextureCoordinatesId',0);this.addField_SFString(ctx,'alphaTextureChannelMask','a');this.addField_SFNode('alphaTexture',x3dom.nodeTypes.X3DTextureNode);this._dirty={};},{nodeChanged:function()
{},fieldChanged:function(fieldName)
{},getDiffuseMap:function()
{if(this._cf.diffuseTexture.node){this._cf.diffuseTexture.node._cf.texture.node._type="diffuseMap";return this._cf.diffuseTexture.node._cf.texture.node;}else{return null;}},getNormalMap:function()
{if(this._cf.normalTexture.node){this._cf.normalTexture.node._cf.texture.node._type="normalMap";return this._cf.normalTexture.node._cf.texture.node;}else{return null;}},getAmbientMap:function()
{if(this._cf.ambientTexture.node){this._cf.ambientTexture.node._cf.texture.node._type="ambientMap";return this._cf.ambientTexture.node._cf.texture.node;}else{return null;}},getSpecularMap:function()
{if(this._cf.specularTexture.node){this._cf.specularTexture.node._cf.texture.node._type="specularMap";return this._cf.specularTexture.node._cf.texture.node;}else{return null;}},getShininessMap:function()
{if(this._cf.shininessTexture.node){this._cf.shininessTexture.node._cf.texture.node._type="shininessMap";return this._cf.shininessTexture.node._cf.texture.node;}else{return null;}},getAlphaMap:function()
{if(this._cf.alphaTexture.node){this._cf.alphaTexture.node._cf.texture.node._type="alphaMap";return this._cf.alphaTexture.node._cf.texture.node;}else{return null;}},getTextures:function()
{var textures=[];var diff=this.getDiffuseMap();if(diff)textures.push(diff);var norm=this.getNormalMap();if(norm)textures.push(norm);var spec=this.getSpecularMap();if(spec)textures.push(spec);return textures;}}));x3dom.registerNodeType("ComposedShader","Shaders",defineClass(x3dom.nodeTypes.X3DShaderNode,function(ctx){x3dom.nodeTypes.ComposedShader.superClass.call(this,ctx);this.addField_MFNode('fields',x3dom.nodeTypes.Field);this.addField_MFNode('parts',x3dom.nodeTypes.ShaderPart);this._vertex=null;this._fragment=null;x3dom.debug.logInfo("Current ComposedShader node implementation limitations:\n"+"Vertex attributes (if given in the standard X3D fields 'coord', 'color', "+"'normal', 'texCoord'), matrices and texture are provided as follows...\n"+"    attribute vec3 position;\n"+"    attribute vec3 normal;\n"+"    attribute vec2 texcoord;\n"+"    attribute vec3 color;\n"+"    uniform mat4 modelViewProjectionMatrix;\n"+"    uniform mat4 modelViewMatrix;\n"+"    uniform mat4 normalMatrix;\n"+"    uniform mat4 viewMatrix;\n"+"    uniform sampler2D tex;\n");},{nodeChanged:function()
{var i,n=this._cf.parts.nodes.length;for(i=0;i<n;i++)
{if(this._cf.parts.nodes[i]._vf.type.toLowerCase()=='vertex'){this._vertex=this._cf.parts.nodes[i];}
else if(this._cf.parts.nodes[i]._vf.type.toLowerCase()=='fragment'){this._fragment=this._cf.parts.nodes[i];}}
var ctx={};n=this._cf.fields.nodes.length;for(i=0;i<n;i++)
{var fieldName=this._cf.fields.nodes[i]._vf.name;ctx.xmlNode=this._cf.fields.nodes[i]._xmlNode;var funcName,func;if(ctx.xmlNode!==undefined&&ctx.xmlNode!==null){ctx.xmlNode.setAttribute(fieldName,this._cf.fields.nodes[i]._vf.value);funcName="this.addField_"+this._cf.fields.nodes[i]._vf.type+"(ctx, name);";func=new Function('ctx','name',funcName);func.call(this,ctx,fieldName);}
else{funcName="this.addField_"+this._cf.fields.nodes[i]._vf.type+"(ctx, name, n);";func=new Function('ctx','name','n',funcName);func.call(this,null,fieldName,this._cf.fields.nodes[i]._vf.value);}}
Array.forEach(this._parentNodes,function(app){Array.forEach(app._parentNodes,function(shape){if(shape._cleanupGLObjects)
shape._cleanupGLObjects();shape.setAllDirty();});});},fieldChanged:function(fieldName)
{var i,n=this._cf.fields.nodes.length;for(i=0;i<n;i++)
{var field=this._cf.fields.nodes[i]._vf.name;if(field===fieldName)
{var msg=this._cf.fields.nodes[i]._vf.value;try{this._vf[field].setValueByStr(msg);}
catch(exc1){try{switch((typeof(this._vf[field])).toString()){case"number":this._vf[field]=+msg;break;case"boolean":this._vf[field]=(msg.toLowerCase()==="true");break;case"string":this._vf[field]=msg;break;}}
catch(exc2){x3dom.debug.logError("setValueByStr() NYI for "+typeof(this._vf[field]));}}
break;}}
if(field==='url')
{Array.forEach(this._parentNodes,function(app){Array.forEach(app._parentNodes,function(shape){shape._dirty.shader=true;});});}},parentAdded:function()
{Array.forEach(this._parentNodes,function(app){app.nodeChanged();});}}));x3dom.registerNodeType("ShaderPart","Shaders",defineClass(x3dom.nodeTypes.X3DNode,function(ctx){x3dom.nodeTypes.ShaderPart.superClass.call(this,ctx);this.addField_MFString(ctx,'url',[]);this.addField_SFString(ctx,'type',"VERTEX");x3dom.debug.assert(this._vf.type.toLowerCase()=='vertex'||this._vf.type.toLowerCase()=='fragment');},{nodeChanged:function()
{var ctx={};ctx.xmlNode=this._xmlNode;if(ctx.xmlNode!==undefined&&ctx.xmlNode!==null)
{var that=this;if(that._vf.url.length&&that._vf.url[0].indexOf('\n')==-1)
{var xhr=new XMLHttpRequest();xhr.open("GET",encodeURI(that._nameSpace.getURL(that._vf.url[0])),false);xhr.onload=function(){that._vf.url=new x3dom.fields.MFString([]);that._vf.url.push(xhr.response);};xhr.onerror=function(){x3dom.debug.logError("Could not load file '"+that._vf.url[0]+"'.");};xhr.send(null);}
else
{if(that._vf.url.length){that._vf.url=new x3dom.fields.MFString([]);}
try{that._vf.url.push(ctx.xmlNode.childNodes[1].nodeValue);ctx.xmlNode.removeChild(ctx.xmlNode.childNodes[1]);}
catch(e){Array.forEach(ctx.xmlNode.childNodes,function(childDomNode){if(childDomNode.nodeType===3){that._vf.url.push(childDomNode.nodeValue);}
else if(childDomNode.nodeType===4){that._vf.url.push(childDomNode.data);}
childDomNode.parentNode.removeChild(childDomNode);});}}}
Array.forEach(this._parentNodes,function(shader){shader.nodeChanged();});},fieldChanged:function(fieldName)
{if(fieldName==="url"){Array.forEach(this._parentNodes,function(shader){shader.fieldChanged("url");});}},parentAdded:function()
{Array.forEach(this._parentNodes,function(shader){shader.nodeChanged();});}}));x3dom.registerNodeType("X3DVertexAttributeNode","Shaders",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.X3DVertexAttributeNode.superClass.call(this,ctx);this.addField_SFString(ctx,'name',"");}));x3dom.registerNodeType("FloatVertexAttribute","Shaders",defineClass(x3dom.nodeTypes.X3DVertexAttributeNode,function(ctx){x3dom.nodeTypes.FloatVertexAttribute.superClass.call(this,ctx);this.addField_SFInt32(ctx,'numComponents',4);this.addField_MFFloat(ctx,'value',[]);},{fieldChanged:function(fieldName){}}));x3dom.registerNodeType("X3DSpatialGeometryNode","Geometry3D",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.X3DSpatialGeometryNode.superClass.call(this,ctx);}));x3dom.registerNodeType("Plane","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.Plane.superClass.call(this,ctx);this.addField_SFVec2f(ctx,'size',2,2);this.addField_SFVec2f(ctx,'subdivision',1,1);this.addField_SFVec3f(ctx,'center',0,0,0);var sx=this._vf.size.x,sy=this._vf.size.y;var subx=this._vf.subdivision.x,suby=this._vf.subdivision.y;var geoCacheID='Plane_'+sx+'-'+sy+'-'+subx+'-'+suby+'-'+this._vf.center.x+'-'+this._vf.center.y+'-'+this._vf.center.z;if(ctx&&this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined)
{this._mesh=x3dom.geoCache[geoCacheID];}
else
{var x=0,y=0;var xstep=sx/subx;var ystep=sy/suby;sx/=2;sy/=2;for(y=0;y<=suby;y++){for(x=0;x<=subx;x++){this._mesh._positions[0].push(this._vf.center.x+x*xstep-sx);this._mesh._positions[0].push(this._vf.center.y+y*ystep-sy);this._mesh._positions[0].push(this._vf.center.z);this._mesh._normals[0].push(0);this._mesh._normals[0].push(0);this._mesh._normals[0].push(1);this._mesh._texCoords[0].push(x/subx);this._mesh._texCoords[0].push(y/suby);}}
for(y=1;y<=suby;y++){for(x=0;x<subx;x++){this._mesh._indices[0].push((y-1)*(subx+1)+x);this._mesh._indices[0].push((y-1)*(subx+1)+x+1);this._mesh._indices[0].push(y*(subx+1)+x);this._mesh._indices[0].push(y*(subx+1)+x);this._mesh._indices[0].push((y-1)*(subx+1)+x+1);this._mesh._indices[0].push(y*(subx+1)+x+1);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;x3dom.geoCache[geoCacheID]=this._mesh;}},{nodeChanged:function(){},fieldChanged:function(fieldName){if(fieldName==="size"){this._mesh._positions[0]=[];var sx=this._vf.size.x,sy=this._vf.size.y;var subx=this._vf.subdivision.x,suby=this._vf.subdivision.y;var x=0,y=0;var xstep=sx/subx;var ystep=sy/suby;sx/=2;sy/=2;for(y=0;y<=suby;y++){for(x=0;x<=subx;x++){this._mesh._positions[0].push(this._vf.center.x+x*xstep-sx);this._mesh._positions[0].push(this._vf.center.y+y*ystep-sy);this._mesh._positions[0].push(this._vf.center.z);}}
this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}else if(fieldName==="subdivision"){this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];var sx=this._vf.size.x,sy=this._vf.size.y;var subx=this._vf.subdivision.x,suby=this._vf.subdivision.y;var x=0,y=0;var xstep=sx/subx;var ystep=sy/suby;sx/=2;sy/=2;for(y=0;y<=suby;y++){for(x=0;x<=subx;x++){this._mesh._positions[0].push(this._vf.center.x+x*xstep-sx);this._mesh._positions[0].push(this._vf.center.y+y*ystep-sy);this._mesh._positions[0].push(this._vf.center.z);this._mesh._normals[0].push(0);this._mesh._normals[0].push(0);this._mesh._normals[0].push(1);this._mesh._texCoords[0].push(x/subx);this._mesh._texCoords[0].push(y/suby);}}
for(y=1;y<=suby;y++){for(x=0;x<subx;x++){this._mesh._indices[0].push((y-1)*(subx+1)+x);this._mesh._indices[0].push((y-1)*(subx+1)+x+1);this._mesh._indices[0].push(y*(subx+1)+x);this._mesh._indices[0].push(y*(subx+1)+x);this._mesh._indices[0].push((y-1)*(subx+1)+x+1);this._mesh._indices[0].push(y*(subx+1)+x+1);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node.setAllDirty();});}}}));x3dom.registerNodeType("ElevationGrid","Geometry3D",defineClass(x3dom.nodeTypes.X3DGeometryNode,function(ctx){x3dom.nodeTypes.ElevationGrid.superClass.call(this,ctx);this.addField_SFBool(ctx,'colorPerVertex',true);this.addField_SFBool(ctx,'normalPerVertex',true);this.addField_SFFloat(ctx,'creaseAngle',0);this.addField_MFNode('attrib',x3dom.nodeTypes.X3DVertexAttributeNode);this.addField_SFNode('normal',x3dom.nodeTypes.Normal);this.addField_SFNode('color',x3dom.nodeTypes.X3DColorNode);this.addField_SFNode('texCoord',x3dom.nodeTypes.X3DTextureCoordinateNode);this.addField_MFFloat(ctx,'height',[]);this.addField_SFInt32(ctx,'xDimension',0);this.addField_SFFloat(ctx,'xSpacing',1.0);this.addField_SFInt32(ctx,'zDimension',0);this.addField_SFFloat(ctx,'zSpacing',1.0);},{nodeChanged:function()
{this._mesh._indices[0]=[];this._mesh._positions[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._colors[0]=[];var x=0,y=0;var subx=this._vf.xDimension-1;var suby=this._vf.zDimension-1;var h=this._vf.height;x3dom.debug.assert((h.length===this._vf.xDimension*this._vf.zDimension));var normals=null,texCoords=null,colors=null;if(this._cf.normal.node){normals=this._cf.normal.node._vf.vector;}
var numTexComponents=2;var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if(texCoordNode){if(texCoordNode._vf.point){texCoords=texCoordNode._vf.point;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}}}
var numColComponents=3;if(this._cf.color.node){colors=this._cf.color.node._vf.color;if(x3dom.isa(this._cf.color.node,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
var c=0;for(y=0;y<=suby;y++)
{for(x=0;x<=subx;x++)
{this._mesh._positions[0].push(x*this._vf.xSpacing);this._mesh._positions[0].push(h[c]);this._mesh._positions[0].push(y*this._vf.zSpacing);if(normals){this._mesh._normals[0].push(normals[c].x);this._mesh._normals[0].push(normals[c].y);this._mesh._normals[0].push(normals[c].z);}
if(texCoords){this._mesh._texCoords[0].push(texCoords[c].x);this._mesh._texCoords[0].push(texCoords[c].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[c].z);}}
else{this._mesh._texCoords[0].push(x/subx);this._mesh._texCoords[0].push(y/suby);}
if(colors){this._mesh._colors[0].push(colors[c].r);this._mesh._colors[0].push(colors[c].g);this._mesh._colors[0].push(colors[c].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c].a);}}
c++;}}
for(y=1;y<=suby;y++){for(x=0;x<subx;x++){this._mesh._indices[0].push((y-1)*(subx+1)+x);this._mesh._indices[0].push(y*(subx+1)+x);this._mesh._indices[0].push((y-1)*(subx+1)+x+1);this._mesh._indices[0].push(y*(subx+1)+x);this._mesh._indices[0].push(y*(subx+1)+x+1);this._mesh._indices[0].push((y-1)*(subx+1)+x+1);}}
if(!normals)
this._mesh.calcNormals(Math.PI);this._mesh._invalidate=true;this._mesh._numTexComponents=numTexComponents;this._mesh._numColComponents=numColComponents;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;},fieldChanged:function(fieldName)
{var normals=null;if(this._cf.normal.node){normals=this._cf.normal.node._vf.vector;}
if(fieldName=="height")
{var i,n=this._mesh._positions[0].length/3;var h=this._vf.height;for(i=0;i<n;i++){this._mesh._positions[0][3*i+1]=h[i];}
if(!normals){this._mesh._normals[0]=[];this._mesh.calcNormals(Math.PI);}
this._mesh._invalidate=true;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;if(!normals)
node._dirty.normals=true;});}}}));x3dom.registerNodeType("Box","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.Box.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'size',2,2,2);var sx=this._vf.size.x,sy=this._vf.size.y,sz=this._vf.size.z;var geoCacheID='Box_'+sx+'-'+sy+'-'+sz;if(this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined)
{this._mesh=x3dom.geoCache[geoCacheID];}
else
{sx/=2;sy/=2;sz/=2;this._mesh._positions[0]=[-sx,-sy,-sz,-sx,sy,-sz,sx,sy,-sz,sx,-sy,-sz,-sx,-sy,sz,-sx,sy,sz,sx,sy,sz,sx,-sy,sz,-sx,-sy,-sz,-sx,-sy,sz,-sx,sy,sz,-sx,sy,-sz,sx,-sy,-sz,sx,-sy,sz,sx,sy,sz,sx,sy,-sz,-sx,sy,-sz,-sx,sy,sz,sx,sy,sz,sx,sy,-sz,-sx,-sy,-sz,-sx,-sy,sz,sx,-sy,sz,sx,-sy,-sz];this._mesh._normals[0]=[0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,1,0,0,1,0,0,1,0,0,1,-1,0,0,-1,0,0,-1,0,0,-1,0,0,1,0,0,1,0,0,1,0,0,1,0,0,0,1,0,0,1,0,0,1,0,0,1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0];this._mesh._texCoords[0]=[1,0,1,1,0,1,0,0,0,0,0,1,1,1,1,0,0,0,1,0,1,1,0,1,1,0,0,0,0,1,1,1,0,1,0,0,1,0,1,1,0,0,0,1,1,1,1,0];this._mesh._indices[0]=[0,1,2,2,3,0,4,7,5,5,7,6,8,9,10,10,11,8,12,14,13,14,12,15,16,17,18,18,19,16,20,22,21,22,20,23];this._mesh._invalidate=true;this._mesh._numFaces=12;this._mesh._numCoords=24;x3dom.geoCache[geoCacheID]=this._mesh;}},{fieldChanged:function(fieldName){if(fieldName==="size"){var sx=this._vf.size.x/2,sy=this._vf.size.y/2,sz=this._vf.size.z/2;this._mesh._positions[0]=[-sx,-sy,-sz,-sx,sy,-sz,sx,sy,-sz,sx,-sy,-sz,-sx,-sy,sz,-sx,sy,sz,sx,sy,sz,sx,-sy,sz,-sx,-sy,-sz,-sx,-sy,sz,-sx,sy,sz,-sx,sy,-sz,sx,-sy,-sz,sx,-sy,sz,sx,sy,sz,sx,sy,-sz,-sx,sy,-sz,-sx,sy,sz,sx,sy,sz,sx,sy,-sz,-sx,-sy,-sz,-sx,-sy,sz,sx,-sy,sz,sx,-sy,-sz];Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}}}));x3dom.registerNodeType("Sphere","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.Sphere.superClass.call(this,ctx);this.addField_SFFloat(ctx,'radius',ctx?1:10000);this.addField_SFVec2f(ctx,'subdivision',24,24);var qfactor=1.0;var r=this._vf.radius;var subx=this._vf.subdivision.x,suby=this._vf.subdivision.y;var geoCacheID='Sphere_'+r;if(this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined){this._mesh=x3dom.geoCache[geoCacheID];}
else{if(ctx){qfactor=ctx.doc.properties.getProperty("PrimitiveQuality","Medium");}
if(!x3dom.Utils.isNumber(qfactor)){switch(qfactor.toLowerCase()){case"low":qfactor=0.3;break;case"medium":qfactor=0.5;break;case"high":qfactor=1.0;break;}}else{qfactor=parseFloat(qfactor);}
this._quality=qfactor;var latNumber,longNumber;var latitudeBands=Math.floor(subx*qfactor);var longitudeBands=Math.floor(suby*qfactor);var theta,sinTheta,cosTheta;var phi,sinPhi,cosPhi;var x,y,z,u,v;for(latNumber=0;latNumber<=latitudeBands;latNumber++){theta=(latNumber*Math.PI)/latitudeBands;sinTheta=Math.sin(theta);cosTheta=Math.cos(theta);for(longNumber=0;longNumber<=longitudeBands;longNumber++){phi=(longNumber*2.0*Math.PI)/longitudeBands;sinPhi=Math.sin(phi);cosPhi=Math.cos(phi);x=-cosPhi*sinTheta;y=-cosTheta;z=-sinPhi*sinTheta;u=0.25-((1.0*longNumber)/longitudeBands);v=latNumber/latitudeBands;this._mesh._positions[0].push(r*x);this._mesh._positions[0].push(r*y);this._mesh._positions[0].push(r*z);this._mesh._normals[0].push(x);this._mesh._normals[0].push(y);this._mesh._normals[0].push(z);this._mesh._texCoords[0].push(u);this._mesh._texCoords[0].push(v);}}
var first,second;for(latNumber=0;latNumber<latitudeBands;latNumber++){for(longNumber=0;longNumber<longitudeBands;longNumber++){first=(latNumber*(longitudeBands+1))+longNumber;second=first+longitudeBands+1;this._mesh._indices[0].push(first);this._mesh._indices[0].push(second);this._mesh._indices[0].push(first+1);this._mesh._indices[0].push(second);this._mesh._indices[0].push(second+1);this._mesh._indices[0].push(first+1);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;x3dom.geoCache[geoCacheID]=this._mesh;}},{fieldChanged:function(fieldName){if(fieldName==="radius"){this._mesh._positions[0]=[];this._mesh._normals[0]=[];var r=this._vf.radius;var subx=this._vf.subdivision.x,suby=this._vf.subdivision.y;var qfactor=this._quality;var latNumber,longNumber;var latitudeBands=Math.floor(subx*qfactor);var longitudeBands=Math.floor(suby*qfactor);var theta,sinTheta,cosTheta;var phi,sinPhi,cosPhi;var x,y,z;for(latNumber=0;latNumber<=latitudeBands;latNumber++){theta=(latNumber*Math.PI)/latitudeBands;sinTheta=Math.sin(theta);cosTheta=Math.cos(theta);for(longNumber=0;longNumber<=longitudeBands;longNumber++){phi=(longNumber*2.0*Math.PI)/longitudeBands;sinPhi=Math.sin(phi);cosPhi=Math.cos(phi);x=-cosPhi*sinTheta;y=-cosTheta;z=-sinPhi*sinTheta;this._mesh._positions[0].push(r*x);this._mesh._positions[0].push(r*y);this._mesh._positions[0].push(r*z);}}
this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}else if(fieldName==="subdivision"){this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];var r=this._vf.radius;var subx=this._vf.subdivision.x,suby=this._vf.subdivision.y;var qfactor=this._quality;var latNumber,longNumber;var latitudeBands=Math.floor(subx*qfactor);var longitudeBands=Math.floor(suby*qfactor);var theta,sinTheta,cosTheta;var phi,sinPhi,cosPhi;var x,y,z,u,v;for(latNumber=0;latNumber<=latitudeBands;latNumber++){theta=(latNumber*Math.PI)/latitudeBands;sinTheta=Math.sin(theta);cosTheta=Math.cos(theta);for(longNumber=0;longNumber<=longitudeBands;longNumber++){phi=(longNumber*2.0*Math.PI)/longitudeBands;sinPhi=Math.sin(phi);cosPhi=Math.cos(phi);x=-cosPhi*sinTheta;y=-cosTheta;z=-sinPhi*sinTheta;u=0.25-((1.0*longNumber)/longitudeBands);v=latNumber/latitudeBands;this._mesh._positions[0].push(r*x);this._mesh._positions[0].push(r*y);this._mesh._positions[0].push(r*z);this._mesh._normals[0].push(x);this._mesh._normals[0].push(y);this._mesh._normals[0].push(z);this._mesh._texCoords[0].push(u);this._mesh._texCoords[0].push(v);}}
var first,second;for(latNumber=0;latNumber<latitudeBands;latNumber++){for(longNumber=0;longNumber<longitudeBands;longNumber++){first=(latNumber*(longitudeBands+1))+longNumber;second=first+longitudeBands+1;this._mesh._indices[0].push(first);this._mesh._indices[0].push(second);this._mesh._indices[0].push(first+1);this._mesh._indices[0].push(second);this._mesh._indices[0].push(second+1);this._mesh._indices[0].push(first+1);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node.setAllDirty();});}}}));x3dom.registerNodeType("Torus","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.Torus.superClass.call(this,ctx);this.addField_SFFloat(ctx,'innerRadius',0.5);this.addField_SFFloat(ctx,'outerRadius',1.0);this.addField_SFVec2f(ctx,'subdivision',24,24);var innerRadius=this._vf.innerRadius;var outerRadius=this._vf.outerRadius;var rings=this._vf.subdivision.x,sides=this._vf.subdivision.y;var geoCacheID='Torus_'+innerRadius+'_'+outerRadius;if(this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined)
{this._mesh=x3dom.geoCache[geoCacheID];}
else
{var ringDelta=2.0*Math.PI/rings;var sideDelta=2.0*Math.PI/sides;var p=[],n=[],t=[],i=[];var a,b,theta,phi;for(a=0,theta=0;a<=rings;a++,theta+=ringDelta)
{var cosTheta=Math.cos(theta);var sinTheta=Math.sin(theta);for(b=0,phi=0;b<=sides;b++,phi+=sideDelta)
{var cosPhi=Math.cos(phi);var sinPhi=Math.sin(phi);var dist=outerRadius+innerRadius*cosPhi;this._mesh._normals[0].push(cosTheta*cosPhi,-sinTheta*cosPhi,sinPhi);this._mesh._positions[0].push(cosTheta*dist,-sinTheta*dist,innerRadius*sinPhi);this._mesh._texCoords[0].push(-a/rings,b/sides);}}
for(a=0;a<sides;a++)
{for(b=0;b<rings;b++)
{this._mesh._indices[0].push(b*(sides+1)+a);this._mesh._indices[0].push(b*(sides+1)+a+1);this._mesh._indices[0].push((b+1)*(sides+1)+a);this._mesh._indices[0].push(b*(sides+1)+a+1);this._mesh._indices[0].push((b+1)*(sides+1)+a+1);this._mesh._indices[0].push((b+1)*(sides+1)+a);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;x3dom.geoCache[geoCacheID]=this._mesh;}},{fieldChanged:function(fieldName){if(fieldName==="innerRadius"||fieldName==="outerRadius"){this._mesh._positions[0]=[];var innerRadius=this._vf.innerRadius;var outerRadius=this._vf.outerRadius;var rings=this._vf.subdivision.x,sides=this._vf.subdivision.y;var ringDelta=2.0*Math.PI/rings;var sideDelta=2.0*Math.PI/sides;var a,b,theta,phi;for(a=0,theta=0;a<=rings;a++,theta+=ringDelta)
{var cosTheta=Math.cos(theta);var sinTheta=Math.sin(theta);for(b=0,phi=0;b<=sides;b++,phi+=sideDelta)
{var cosPhi=Math.cos(phi);var sinPhi=Math.sin(phi);var dist=outerRadius+innerRadius*cosPhi;this._mesh._positions[0].push(cosTheta*dist,-sinTheta*dist,innerRadius*sinPhi);}}
this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}else if(fieldName==="subdivision"){this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];var innerRadius=this._vf.innerRadius;var outerRadius=this._vf.outerRadius;var rings=this._vf.subdivision.x,sides=this._vf.subdivision.y;var ringDelta=2.0*Math.PI/rings;var sideDelta=2.0*Math.PI/sides;var a,b,theta,phi;for(a=0,theta=0;a<=rings;a++,theta+=ringDelta)
{var cosTheta=Math.cos(theta);var sinTheta=Math.sin(theta);for(b=0,phi=0;b<=sides;b++,phi+=sideDelta)
{var cosPhi=Math.cos(phi);var sinPhi=Math.sin(phi);var dist=outerRadius+innerRadius*cosPhi;this._mesh._normals[0].push(cosTheta*cosPhi,-sinTheta*cosPhi,sinPhi);this._mesh._positions[0].push(cosTheta*dist,-sinTheta*dist,innerRadius*sinPhi);this._mesh._texCoords[0].push(-a/rings,b/sides);}}
for(a=0;a<sides;a++)
{for(b=0;b<rings;b++)
{this._mesh._indices[0].push(b*(sides+1)+a);this._mesh._indices[0].push(b*(sides+1)+a+1);this._mesh._indices[0].push((b+1)*(sides+1)+a);this._mesh._indices[0].push(b*(sides+1)+a+1);this._mesh._indices[0].push((b+1)*(sides+1)+a+1);this._mesh._indices[0].push((b+1)*(sides+1)+a);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node.setAllDirty();});}}}));x3dom.registerNodeType("Cone","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.Cone.superClass.call(this,ctx);this.addField_SFFloat(ctx,'bottomRadius',1.0);this.addField_SFFloat(ctx,'height',2.0);this.addField_SFBool(ctx,'bottom',true);this.addField_SFFloat(ctx,'subdivision',32);this.addField_SFBool(ctx,'side',true);var sides=this._vf.subdivision;var geoCacheID='Cone_'+this._vf.bottomRadius+'_'+this._vf.height+'_'+this._vf.bottom+'_'+this._vf.side;if(this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined)
{this._mesh=x3dom.geoCache[geoCacheID];}
else
{var bottomRadius=this._vf.bottomRadius,height=this._vf.height;var beta,x,z;var delta=2.0*Math.PI/sides;var incl=bottomRadius/height;var nlen=1.0/Math.sqrt(1.0+incl*incl);var j=0;var k=0;if(this._vf.side)
{for(j=0,k=0;j<=sides;j++)
{beta=j*delta;x=Math.sin(beta);z=-Math.cos(beta);this._mesh._positions[0].push(0,height/2,0);this._mesh._normals[0].push(x/nlen,incl/nlen,z/nlen);this._mesh._texCoords[0].push(1.0-j/sides,1);this._mesh._positions[0].push(x*bottomRadius,-height/2,z*bottomRadius);this._mesh._normals[0].push(x/nlen,incl/nlen,z/nlen);this._mesh._texCoords[0].push(1.0-j/sides,0);if(j>0)
{this._mesh._indices[0].push(k+0);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+3);k+=2;}}}
if(this._vf.bottom&&bottomRadius>0)
{var base=this._mesh._positions[0].length/3;for(j=sides-1;j>=0;j--)
{beta=j*delta;x=bottomRadius*Math.sin(beta);z=-bottomRadius*Math.cos(beta);this._mesh._positions[0].push(x,-height/2,z);this._mesh._normals[0].push(0,-1,0);this._mesh._texCoords[0].push(x/bottomRadius/2+0.5,z/bottomRadius/2+0.5);}
var h=base+1;for(j=2;j<sides;j++)
{this._mesh._indices[0].push(h);this._mesh._indices[0].push(base);h=base+j;this._mesh._indices[0].push(h);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;x3dom.geoCache[geoCacheID]=this._mesh;}},{fieldChanged:function(fieldName){if(fieldName==="bottomRadius"||fieldName==="height"){this._mesh._positions[0]=[];var bottomRadius=this._vf.bottomRadius,height=this._vf.height;var sides=this._vf.subdivision;var beta,x,z;var delta=2.0*Math.PI/sides;var incl=bottomRadius/height;var nlen=1.0/Math.sqrt(1.0+incl*incl);if(this._vf.side)
{for(var j=0;j<=sides;j++)
{beta=j*delta;x=Math.sin(beta);z=-Math.cos(beta);this._mesh._positions[0].push(0,height/2,0);this._mesh._positions[0].push(x*bottomRadius,-height/2,z*bottomRadius);}}
if(this._vf.bottom&&bottomRadius>0)
{var base=this._mesh._positions[0].length/3;for(var j=sides-1;j>=0;j--)
{beta=j*delta;x=bottomRadius*Math.sin(beta);z=-bottomRadius*Math.cos(beta);this._mesh._positions[0].push(x,-height/2,z);}}
this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}else if(fieldName==="subdivision"||fieldName==="bottom"){this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];var bottomRadius=this._vf.bottomRadius,height=this._vf.height;var sides=this._vf.subdivision;var beta,x,z;var delta=2.0*Math.PI/sides;var incl=bottomRadius/height;var nlen=1.0/Math.sqrt(1.0+incl*incl);var j=0;var k=0;if(this._vf.side)
{for(j=0,k=0;j<=sides;j++)
{beta=j*delta;x=Math.sin(beta);z=-Math.cos(beta);this._mesh._positions[0].push(0,height/2,0);this._mesh._normals[0].push(x/nlen,incl/nlen,z/nlen);this._mesh._texCoords[0].push(1.0-j/sides,1);this._mesh._positions[0].push(x*bottomRadius,-height/2,z*bottomRadius);this._mesh._normals[0].push(x/nlen,incl/nlen,z/nlen);this._mesh._texCoords[0].push(1.0-j/sides,0);if(j>0)
{this._mesh._indices[0].push(k+0);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+3);k+=2;}}}
if(this._vf.bottom&&bottomRadius>0)
{var base=this._mesh._positions[0].length/3;for(j=sides-1;j>=0;j--)
{beta=j*delta;x=bottomRadius*Math.sin(beta);z=-bottomRadius*Math.cos(beta);this._mesh._positions[0].push(x,-height/2,z);this._mesh._normals[0].push(0,-1,0);this._mesh._texCoords[0].push(x/bottomRadius/2+0.5,z/bottomRadius/2+0.5);}
var h=base+1;for(j=2;j<sides;j++)
{this._mesh._indices[0].push(h);this._mesh._indices[0].push(base);h=base+j;this._mesh._indices[0].push(h);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node.setAllDirty();});}}}));x3dom.registerNodeType("Cylinder","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.Cylinder.superClass.call(this,ctx);this.addField_SFFloat(ctx,'radius',1.0);this.addField_SFFloat(ctx,'height',2.0);this.addField_SFBool(ctx,'bottom',true);this.addField_SFBool(ctx,'top',true);this.addField_SFFloat(ctx,'subdivision',32);this.addField_SFBool(ctx,'side',true);var sides=this._vf.subdivision;var geoCacheID='Cylinder_'+this._vf.radius+'_'+this._vf.height+'_'+this._vf.bottom+'_'+this._vf.top+'_'+this._vf.side;if(this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined)
{this._mesh=x3dom.geoCache[geoCacheID];}
else
{var radius=this._vf.radius;var height=this._vf.height;var beta,x,z;var delta=2.0*Math.PI/sides;var j=0;var k=0;if(this._vf.side)
{for(j=0,k=0;j<=sides;j++)
{beta=j*delta;x=Math.sin(beta);z=-Math.cos(beta);this._mesh._positions[0].push(x*radius,-height/2,z*radius);this._mesh._normals[0].push(x,0,z);this._mesh._texCoords[0].push(1.0-j/sides,0);this._mesh._positions[0].push(x*radius,height/2,z*radius);this._mesh._normals[0].push(x,0,z);this._mesh._texCoords[0].push(1.0-j/sides,1);if(j>0)
{this._mesh._indices[0].push(k+0);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+3);k+=2;}}}
if(radius>0)
{var h,base=this._mesh._positions[0].length/3;if(this._vf.top)
{for(j=sides-1;j>=0;j--)
{beta=j*delta;x=radius*Math.sin(beta);z=-radius*Math.cos(beta);this._mesh._positions[0].push(x,height/2,z);this._mesh._normals[0].push(0,1,0);this._mesh._texCoords[0].push(x/radius/2+0.5,-z/radius/2+0.5);}
h=base+1;for(j=2;j<sides;j++)
{this._mesh._indices[0].push(base);this._mesh._indices[0].push(h);h=base+j;this._mesh._indices[0].push(h);}
base=this._mesh._positions[0].length/3;}
if(this._vf.bottom)
{for(j=sides-1;j>=0;j--)
{beta=j*delta;x=radius*Math.sin(beta);z=-radius*Math.cos(beta);this._mesh._positions[0].push(x,-height/2,z);this._mesh._normals[0].push(0,-1,0);this._mesh._texCoords[0].push(x/radius/2+0.5,z/radius/2+0.5);}
h=base+1;for(j=2;j<sides;j++)
{this._mesh._indices[0].push(h);this._mesh._indices[0].push(base);h=base+j;this._mesh._indices[0].push(h);}}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;x3dom.geoCache[geoCacheID]=this._mesh;}},{fieldChanged:function(fieldName){if(fieldName==="radius"||fieldName==="height"){this._mesh._positions[0]=[];var radius=this._vf.radius,height=this._vf.height;var sides=this._vf.subdivision;var beta,x,z;var delta=2.0*Math.PI/sides;var j=0;if(this._vf.side)
{for(j=0;j<=sides;j++)
{beta=j*delta;x=Math.sin(beta);z=-Math.cos(beta);this._mesh._positions[0].push(x*radius,-height/2,z*radius);this._mesh._positions[0].push(x*radius,height/2,z*radius);}}
if(radius>0)
{var h,base=this._mesh._positions[0].length/3;if(this._vf.top)
{for(j=sides-1;j>=0;j--)
{beta=j*delta;x=radius*Math.sin(beta);z=-radius*Math.cos(beta);this._mesh._positions[0].push(x,height/2,z);}}}
if(this._vf.bottom)
{for(j=sides-1;j>=0;j--)
{beta=j*delta;x=radius*Math.sin(beta);z=-radius*Math.cos(beta);this._mesh._positions[0].push(x,-height/2,z);}}
this._mesh._invalidate=true;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}else if(fieldName==="subdivision"||fieldName==="bottom"||fieldName==="top"){this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];var radius=this._vf.radius,height=this._vf.height;var sides=this._vf.subdivision;var beta,x,z;var delta=2.0*Math.PI/sides;var j=0;var k=0;if(this._vf.side)
{for(j=0,k=0;j<=sides;j++)
{beta=j*delta;x=Math.sin(beta);z=-Math.cos(beta);this._mesh._positions[0].push(x*radius,-height/2,z*radius);this._mesh._normals[0].push(x,0,z);this._mesh._texCoords[0].push(1.0-j/sides,0);this._mesh._positions[0].push(x*radius,height/2,z*radius);this._mesh._normals[0].push(x,0,z);this._mesh._texCoords[0].push(1.0-j/sides,1);if(j>0)
{this._mesh._indices[0].push(k+0);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+2);this._mesh._indices[0].push(k+1);this._mesh._indices[0].push(k+3);k+=2;}}}
if(radius>0)
{var h,base=this._mesh._positions[0].length/3;if(this._vf.top)
{for(j=sides-1;j>=0;j--)
{beta=j*delta;x=radius*Math.sin(beta);z=-radius*Math.cos(beta);this._mesh._positions[0].push(x,height/2,z);this._mesh._normals[0].push(0,1,0);this._mesh._texCoords[0].push(x/radius/2+0.5,-z/radius/2+0.5);}
h=base+1;for(j=2;j<sides;j++)
{this._mesh._indices[0].push(base);this._mesh._indices[0].push(h);h=base+j;this._mesh._indices[0].push(h);}
base=this._mesh._positions[0].length/3;}
if(this._vf.bottom)
{for(j=sides-1;j>=0;j--)
{beta=j*delta;x=radius*Math.sin(beta);z=-radius*Math.cos(beta);this._mesh._positions[0].push(x,-height/2,z);this._mesh._normals[0].push(0,-1,0);this._mesh._texCoords[0].push(x/radius/2+0.5,z/radius/2+0.5);}
h=base+1;for(j=2;j<sides;j++)
{this._mesh._indices[0].push(h);this._mesh._indices[0].push(base);h=base+j;this._mesh._indices[0].push(h);}}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;Array.forEach(this._parentNodes,function(node){node.setAllDirty();});}}}));x3dom.registerNodeType("X3DBinaryContainerGeometryNode","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.X3DBinaryContainerGeometryNode.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'position',0,0,0);this.addField_SFVec3f(ctx,'size',1,1,1);this.addField_MFInt32(ctx,'vertexCount',[0]);this.addField_MFString(ctx,'primType',['TRIANGLES']);this._mesh._invalidate=false;this._mesh._numCoords=0;this._mesh._numFaces=0;this._diameter=this._vf.size.length();},{getMin:function(){var vol=this._mesh._vol;if(!vol.isValid()){vol.setBoundsByCenterSize(this._vf.position,this._vf.size);}
return vol.min;},getMax:function(){var vol=this._mesh._vol;if(!vol.isValid()){vol.setBoundsByCenterSize(this._vf.position,this._vf.size);}
return vol.max;},getVolume:function(min,max){var vol=this._mesh._vol;if(!vol.isValid()){vol.setBoundsByCenterSize(this._vf.position,this._vf.size);}
vol.getBounds(min,max);return true;},invalidateVolume:function(){},getCenter:function(){return this._vf.position;},getDiameter:function(){return this._diameter;}}));x3dom.registerNodeType("BinaryGeometry","Geometry3D",defineClass(x3dom.nodeTypes.X3DBinaryContainerGeometryNode,function(ctx){x3dom.nodeTypes.BinaryGeometry.superClass.call(this,ctx);this.addField_SFString(ctx,'index',"");this.addField_SFString(ctx,'coord',"");this.addField_SFString(ctx,'normal',"");this.addField_SFString(ctx,'texCoord',"");this.addField_SFString(ctx,'color',"");this.addField_SFString(ctx,'tangent',"");this.addField_SFString(ctx,'binormal',"");this.addField_SFString(ctx,'indexType',"Uint16");this.addField_SFString(ctx,'coordType',"Float32");this.addField_SFString(ctx,'normalType',"Float32");this.addField_SFString(ctx,'texCoordType',"Float32");this.addField_SFString(ctx,'colorType',"Float32");this.addField_SFString(ctx,'tangentType',"Float32");this.addField_SFString(ctx,'binormalType',"Float32");this.addField_SFBool(ctx,'normalAsSphericalCoordinates',false);this.addField_SFBool(ctx,'rgbaColors',false);this.addField_SFInt32(ctx,'numTexCoordComponents',2);this.addField_SFBool(ctx,'normalPerVertex',true);this.addField_SFBool(ctx,'idsPerVertex',false);this._hasStrideOffset=false;this._mesh._numPosComponents=this._vf.normalAsSphericalCoordinates?4:3;this._mesh._numTexComponents=this._vf.numTexCoordComponents;this._mesh._numColComponents=this._vf.rgbaColors?4:3;this._mesh._numNormComponents=this._vf.normalAsSphericalCoordinates?2:3;this._vertexCountSum=0;for(var i=0;i<this._vf.vertexCount.length;++i){this._vertexCountSum+=this._vf.vertexCount[i];}},{nodeChanged:function()
{},parentAdded:function()
{var offsetInd,strideInd,offset,stride;offsetInd=this._vf.coord.lastIndexOf('#');strideInd=this._vf.coord.lastIndexOf('+');if(offsetInd>=0&&strideInd>=0){offset=+this._vf.coord.substring(++offsetInd,strideInd);stride=+this._vf.coord.substring(strideInd);this._parentNodes[0]._coordStrideOffset=[stride,offset];this._hasStrideOffset=true;if((offset/8)-Math.floor(offset/8)==0){this._mesh._numPosComponents=4;}}
else if(strideInd>=0){stride=+this._vf.coord.substring(strideInd);this._parentNodes[0]._coordStrideOffset=[stride,0];if((stride/8)-Math.floor(stride/8)==0){this._mesh._numPosComponents=4;}}
offsetInd=this._vf.normal.lastIndexOf('#');strideInd=this._vf.normal.lastIndexOf('+');if(offsetInd>=0&&strideInd>=0){offset=+this._vf.normal.substring(++offsetInd,strideInd);stride=+this._vf.normal.substring(strideInd);this._parentNodes[0]._normalStrideOffset=[stride,offset];}
else if(strideInd>=0){stride=+this._vf.normal.substring(strideInd);this._parentNodes[0]._normalStrideOffset=[stride,0];}
offsetInd=this._vf.texCoord.lastIndexOf('#');strideInd=this._vf.texCoord.lastIndexOf('+');if(offsetInd>=0&&strideInd>=0){offset=+this._vf.texCoord.substring(++offsetInd,strideInd);stride=+this._vf.texCoord.substring(strideInd);this._parentNodes[0]._texCoordStrideOffset=[stride,offset];}
else if(strideInd>=0){stride=+this._vf.texCoord.substring(strideInd);this._parentNodes[0]._texCoordStrideOffset=[stride,0];}
offsetInd=this._vf.color.lastIndexOf('#');strideInd=this._vf.color.lastIndexOf('+');if(offsetInd>=0&&strideInd>=0){offset=+this._vf.color.substring(++offsetInd,strideInd);stride=+this._vf.color.substring(strideInd);this._parentNodes[0]._colorStrideOffset=[stride,offset];}
else if(strideInd>=0){stride=+this._vf.color.substring(strideInd);this._parentNodes[0]._colorStrideOffset=[stride,0];}
if(this._vf.indexType!="Uint16")
x3dom.debug.logWarning("Index type "+this._vf.indexType+" problematic");},doIntersect:function(line)
{if(this._pickable){var min=this.getMin();var max=this.getMax();var isect=line.intersect(min,max);if(isect&&line.enter<line.dist){line.dist=line.enter;line.hitObject=this;line.hitPoint=line.pos.add(line.dir.multiply(line.enter));return true;}
else{return false;}}
return false;},getPrecisionMax:function(type)
{switch(this._vf[type])
{case"Int8":return 127.0;case"Uint8":return 255.0;case"Int16":return 32767.0;case"Uint16":return 65535.0;case"Int32":return 2147483647.0;case"Uint32":return 4294967295.0;case"Float32":case"Float64":default:return 1.0;}}}));x3dom.registerNodeType("PopGeometryLevel","Geometry3D",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.PopGeometryLevel.superClass.call(this,ctx);this.addField_SFString(ctx,'src',"");this.addField_SFInt32(ctx,'numIndices',0);this.addField_SFInt32(ctx,'vertexDataBufferOffset',0);},{nodeChanged:function(){},fieldChanged:function(fieldName){},getSrc:function(){return this._vf.src;},getNumIndices:function(){return this._vf.numIndices;},getVertexDataBufferOffset:function(){return this._vf.vertexDataBufferOffset;}}));x3dom.registerNodeType("PopGeometry","Geometry3D",defineClass(x3dom.nodeTypes.X3DBinaryContainerGeometryNode,function(ctx){x3dom.nodeTypes.PopGeometry.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'tightSize',1,1,1);this.addField_SFVec3f(ctx,'bbMinModF',0,0,0);this.addField_SFVec3f(ctx,'bbMaxModF',1,1,1);this.addField_SFVec3f(ctx,'bbMin',0,0,0);this.addField_SFVec3f(ctx,'bbShiftVec',0,0,0);if(this._vf.bbMinModF.x>=this._vf.bbMaxModF.x)
this._vf.bbShiftVec.x=1.0;if(this._vf.bbMinModF.y>=this._vf.bbMaxModF.y)
this._vf.bbShiftVec.y=1.0;if(this._vf.bbMinModF.z>=this._vf.bbMaxModF.z)
this._vf.bbShiftVec.z=1.0;this.addField_MFNode('levels',x3dom.nodeTypes.PopGeometryLevel);this.addField_SFInt32(ctx,'attributeStride',0);this.addField_SFInt32(ctx,'positionOffset',0);this.addField_SFInt32(ctx,'normalOffset',0);this.addField_SFInt32(ctx,'texcoordOffset',0);this.addField_SFInt32(ctx,'colorOffset',0);this.addField_SFInt32(ctx,'numAnchorVertices',0);this.addField_SFInt32(ctx,'positionPrecision',2);this.addField_SFInt32(ctx,'normalPrecision',1);this.addField_SFInt32(ctx,'texcoordPrecision',2);this.addField_SFInt32(ctx,'colorPrecision',1);this.addField_SFInt32(ctx,'minPrecisionLevel',-1);this.addField_SFInt32(ctx,'maxPrecisionLevel',-1);this.addField_SFFloat(ctx,'precisionFactor',1.0);this.addField_SFString(ctx,'coordType',"Uint16");this.addField_SFString(ctx,'normalType',"Uint8");this.addField_SFString(ctx,'texCoordType',"Uint16");this.addField_SFString(ctx,'colorType',"Uint8");this.addField_SFInt32(ctx,'vertexBufferSize',0);this.addField_SFBool(ctx,'indexedRendering',false);this.addField_SFBool(ctx,'sphericalNormals',false);this.addField_MFInt32(ctx,'originalVertexCount',[0]);for(var i=0;i<this._vf.vertexCount.length;++i){this._vf.originalVertexCount[i]=this._vf.vertexCount[i];}
this._bbMinBySize=[Math.floor(this._vf.bbMin.x/this._vf.size.x),Math.floor(this._vf.bbMin.y/this._vf.size.y),Math.floor(this._vf.bbMin.z/this._vf.size.z)];this._volRadius=this._vf.tightSize.length()/2;this._volLargestRadius=this._vf.size.length()/2;this._mesh._numPosComponents=this._vf.sphericalNormals?4:3;this._mesh._numNormComponents=this._vf.sphericalNormals?2:3;this._mesh._numTexComponents=2;this._mesh._numColComponents=3;x3dom.nodeTypes.PopGeometry.numTotalVerts+=this.getVertexCount();x3dom.nodeTypes.PopGeometry.numTotalTris+=(this.hasIndex()?this.getTotalNumberOfIndices():this.getVertexCount())/3;},{nodeChanged:function(){},parentAdded:function(){},getBBoxShiftVec:function(){return this._vf.bbShiftVec;},getBBoxSize:function(){return this._vf.size;},getDiameter:function(){return this._volLargestRadius*2;},hasIndex:function(){return this._vf.indexedRendering;},getTotalNumberOfIndices:function(){if(this._vf.indexedRendering){var sum=0;for(var i=0;i<this._vf.originalVertexCount.length;++i){sum+=this._vf.originalVertexCount[i];}
return sum;}
else{return 0;}},getVertexCount:function(){var sum=0;for(var i=0;i<this._vf.originalVertexCount.length;++i){sum+=this._vf.originalVertexCount[i];}
return sum;},adaptVertexCount:function(numVerts){var verts=0;for(var i=0;i<this._vf.originalVertexCount.length;++i){if((this._vf.originalVertexCount[i]+verts)<=numVerts){this._vf.vertexCount[i]=this._vf.originalVertexCount[i];verts+=this._vf.originalVertexCount[i];}
else{this._vf.vertexCount[i]=numVerts-verts;break;}}},hasNormal:function(){return(this._vf.normalOffset!=0)&&!this._vf.sphericalNormals;},hasTexCoord:function(){return(this._vf.texcoordOffset!=0);},hasColor:function(){return(this._vf.colorOffset!=0);},getPositionPrecision:function(){return this._vf.positionPrecision;},getNormalPrecision:function(){return this._vf.normalPrecision;},getTexCoordPrecision:function(){return this._vf.texcoordPrecision;},getColorPrecision:function(){return this._vf.colorPrecision;},getAttributeStride:function(){return this._vf.attributeStride;},getPositionOffset:function(){return this._vf.positionOffset;},getNormalOffset:function(){return this._vf.normalOffset;},getTexCoordOffset:function(){return this._vf.texcoordOffset;},getColorOffset:function(){return this._vf.colorOffset;},getBufferTypeStringFromByteCount:function(bytes){switch(bytes)
{case 1:return"Uint8";case 2:return"Uint16";default:return 0;}},getDataURLs:function(){var urls=[];for(var i=0;i<this._cf.levels.nodes.length;++i){urls.push(this._cf.levels.nodes[i].getSrc());}
return urls;},getNumIndicesByLevel:function(lvl){return this._cf.levels.nodes[lvl].getNumIndices();},getNumLevels:function(lvl){return this._cf.levels.nodes.length;},getVertexDataBufferOffset:function(lvl){return this._cf.levels.nodes[lvl].getVertexDataBufferOffset();},getPrecisionMax:function(type){switch(this._vf[type])
{case"Uint8":return 255.0;case"Uint16":return 65535.0;default:return 1.0;}}}));x3dom.nodeTypes.PopGeometry.ErrorToleranceFactor=1;x3dom.nodeTypes.PopGeometry.PrecisionFactorOnMove=1;x3dom.nodeTypes.PopGeometry.numRenderedVerts=0;x3dom.nodeTypes.PopGeometry.numRenderedTris=0;x3dom.nodeTypes.PopGeometry.numTotalVerts=0;x3dom.nodeTypes.PopGeometry.numTotalTris=0;x3dom.nodeTypes.PopGeometry.powLUT=[32768,16384,8192,4096,2048,1024,512,256,128,64,32,16,8,4,2,1];x3dom.registerNodeType("BitLODGeoComponent","Geometry3D",defineClass(x3dom.nodeTypes.X3DGeometricPropertyNode,function(ctx){x3dom.nodeTypes.BitLODGeoComponent.superClass.call(this,ctx);this.addField_SFString(ctx,'src',"");this.addField_MFInt32(ctx,'format',[]);this.addField_MFString(ctx,'attrib',[]);this._attribShift=[];this._attribShiftDec=[];this._mask=[];this._bitsPerComponent=0;},{nodeChanged:function()
{for(var f=0;f<this._vf.format.length;f++){this._bitsPerComponent+=this._vf.format[f];}},fieldChanged:function(fieldName)
{},getSrc:function()
{return this._vf.src;},getFormat:function()
{return this._vf.format;},getAttrib:function(idx)
{return this._vf.attrib[idx];},getNumAttribs:function()
{return this._vf.attrib.length;}}));x3dom.registerNodeType("BitLODGeometry","Geometry3D",defineClass(x3dom.nodeTypes.X3DBinaryContainerGeometryNode,function(ctx){x3dom.nodeTypes.BitLODGeometry.superClass.call(this,ctx);this.addField_SFString(ctx,'index',"");this.addField_SFBool(ctx,'usesVLCIndices',false);this.addField_SFBool(ctx,'normalAsSphericalCoordinates',false);this.addField_SFBool(ctx,'normalPerVertex',true);this.addField_MFNode('components',x3dom.nodeTypes.BitLODGeoComponent);this.addField_SFString(ctx,'coordType',"Uint16");this.addField_SFString(ctx,'normalType',"Uint16");this.addField_SFString(ctx,'texCoordType',"Uint16");this.addField_SFString(ctx,'colorType',"Uint16");this._hasStrideOffset=false;this._mesh._numTexComponents=2;this._mesh._numColComponents=3;this._vf.normalPerVertex=!this._vf.usesVLCIndices;this._vf.normalAsSphericalCoordinates=true;this._mesh._numNormComponents=this._vf.normalAsSphericalCoordinates?2:3;},{nodeChanged:function()
{},parentAdded:function()
{this._parentNodes[0]._coordStrideOffset=[12,0];this._parentNodes[0]._normalStrideOffset=[12,8];this._parentNodes[0]._texCoordStrideOffset=[4,0];this._parentNodes[0]._colorStrideOffset=[6,0];},fieldChanged:function(fieldName)
{},hasIndex:function()
{return(this._vf.index.length)?true:false;},usesVLCIndices:function()
{return this._vf.usesVLCIndices==true;},hasColor:function()
{for(var i=0;i<this.getNumComponents();i++){for(var j=0;j<this.getComponent(i).getNumAttribs();j++){if(this.getComponent(i).getAttrib(j)=="color3")
return true;}}
return false;},hasTexCoord:function()
{for(var i=0;i<this.getNumComponents();i++){for(var j=0;j<this.getComponent(i).getNumAttribs();j++){if(this.getComponent(i).getAttrib(j)=="texcoord2")
return true;}}
return false;},getCoordNormalURLs:function(){var coordNormalURLs=[];for(var i=0;i<this.getNumComponents();i++){for(var j=0;j<this.getComponent(i).getNumAttribs();j++){if(this.getComponent(i).getAttrib(j)=="coord3"){coordNormalURLs.push(this.getComponent(i).getSrc());}}}
return coordNormalURLs;},getTexCoordURLs:function(){var texCoordURLs=[];for(var i=0;i<this.getNumComponents();i++){for(var j=0;j<this.getComponent(i).getNumAttribs();j++){if(this.getComponent(i).getAttrib(j)=="texcoord2"){texCoordURLs.push(this.getComponent(i).getSrc());}}}
return texCoordURLs;},getColorURLs:function(){var colorURLs=[];for(var i=0;i<this.getNumComponents();i++){for(var j=0;j<this.getComponent(i).getNumAttribs();j++){if(this.getComponent(i).getAttrib(j)=="color3"){colorURLs.push(this.getComponent(i).getSrc());}}}
return colorURLs;},getNumPrimTypes:function()
{return this._vf.primType.length;},getPrimType:function(idx)
{if(idx<this.getNumPrimTypes())
return this._vf.primType[idx].toUpperCase();return"";},getNumVertexCounts:function()
{return this._vf.vertexCount.length;},getVertexCount:function(idx)
{if(idx<this.getNumVertexCounts())
return this._vf.vertexCount[idx];return 0;},setVertexCount:function(idx,value)
{this._vf.vertexCount[idx]=value;},getNumComponents:function()
{return this._cf.components.nodes.length;},getComponent:function(idx)
{return this._cf.components.nodes[idx];},getComponentsURLs:function()
{var URLs=[];for(var c=0;c<this._cf.components.nodes.length;c++)
URLs[c]=this._cf.components.nodes[c].getSrc();return URLs;},getComponentFormats:function()
{var formats=[];for(var c=0;c<this._cf.components.nodes.length;c++)
formats[c]=this._cf.components.nodes[c]._vf.format;return formats;},getComponentAttribs:function()
{var attribs=[];for(var c=0;c<this._cf.components.nodes.length;c++)
attribs[c]=this._cf.components.nodes[c]._vf.attrib;return attribs;},getNumVertices:function()
{var count=0;for(var i=0;i<this._vf.vertexCount.length;i++){count+=this._vf.vertexCount[i];}
return count;},getAttribType:function(bits)
{switch(bits)
{case 8:return"Uint8";case 16:return"Uint16";case 32:return"Float32";default:return 0;}},getPrecisionMax:function(type)
{switch(this._vf[type])
{case"Int8":return 127.0;case"Uint8":return 255.0-(Math.pow(2.0,8.0-this.loadedLevels)-1.0);case"Int16":return 32767.0;case"Uint16":if(type==='normalType')
return 65535.0-(Math.pow(2.0,16.0-this.loadedLevels)-1.0);else
return 65535.0-(Math.pow(2.0,16.0-this.loadedLevels*2.0)-1.0);case"Int32":return 2147483647.0;case"Uint32":return 4294967295.0;case"Float32":case"Float64":default:return 1.0;}}}));x3dom.registerNodeType("ImageGeometry","Geometry3D",defineClass(x3dom.nodeTypes.X3DBinaryContainerGeometryNode,function(ctx){x3dom.nodeTypes.ImageGeometry.superClass.call(this,ctx);this.addField_SFVec2f(ctx,'implicitMeshSize',256,256);this.addField_SFInt32(ctx,'numColorComponents',3);this.addField_SFInt32(ctx,'numTexCoordComponents',2);this.addField_SFNode('index',x3dom.nodeTypes.X3DTextureNode);this.addField_MFNode('coord',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('normal',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('texCoord',x3dom.nodeTypes.X3DTextureNode);this.addField_SFNode('color',x3dom.nodeTypes.X3DTextureNode);this._mesh._numColComponents=this._vf.numColorComponents;this._mesh._numTexComponents=this._vf.numTexCoordComponents;if(this._vf.implicitMeshSize.y==0)
this._vf.implicitMeshSize.y=this._vf.implicitMeshSize.x;if(x3dom.caps.BACKEND=='webgl'&&x3dom.caps.MAX_VERTEX_TEXTURE_IMAGE_UNITS>0){var geoCacheID='ImageGeometry';if(this._vf.useGeoCache&&x3dom.geoCache[geoCacheID]!==undefined)
{this._mesh=x3dom.geoCache[geoCacheID];}
else
{for(var y=0;y<this._vf.implicitMeshSize.y;y++)
{for(var x=0;x<this._vf.implicitMeshSize.x;x++)
{this._mesh._positions[0].push(x/this._vf.implicitMeshSize.x,y/this._vf.implicitMeshSize.y,0);}}
this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;x3dom.geoCache[geoCacheID]=this._mesh;}}
this._vol=new x3dom.fields.BoxVolume();this._dirty={coord:true,normal:true,texCoord:true,color:true,index:true};},{nodeChanged:function()
{Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;node._dirty.normals=true;node._dirty.texcoords=true;node._dirty.colors=true;});this._vol.invalidate();},fieldChanged:function(fieldName)
{if(fieldName=="coord"||fieldName=="normal"||fieldName=="texCoord"||fieldName=="color"||fieldName=="index"){this._dirty[fieldName]=true;}
this._vol.invalidate();},getMin:function(){var vol=this._vol;if(!vol.isValid()){vol.setBoundsByCenterSize(this._vf.position,this._vf.size);}
return vol.min;},getMax:function(){var vol=this._vol;if(!vol.isValid()){vol.setBoundsByCenterSize(this._vf.position,this._vf.size);}
return vol.max;},getVolume:function(min,max){var vol=this._vol;if(!vol.isValid()){vol.setBoundsByCenterSize(this._vf.position,this._vf.size);}
vol.getBounds(min,max);return true;},numCoordinateTextures:function()
{return this._cf.coord.nodes.length;},getIndexTexture:function()
{if(this._cf.index.node){this._cf.index.node._type="IG_index";return this._cf.index.node;}else{return null;}},getIndexTextureURL:function()
{if(this._cf.index.node){return this._cf.index.node._vf.url;}else{return null;}},getCoordinateTexture:function(pos)
{if(this._cf.coord.nodes[pos]){this._cf.coord.nodes[pos]._type="IG_coords"+pos;return this._cf.coord.nodes[pos];}else{return null;}},getCoordinateTextureURL:function(pos)
{if(this._cf.coord.nodes[pos]){return this._cf.coord.nodes[pos]._vf.url;}else{return null;}},getCoordinateTextureURLs:function()
{var urls=[];for(var i=0;i<this._cf.coord.nodes.length;i++)
{urls.push(this._cf.coord.nodes[i]._vf.url);}
return urls;},getNormalTexture:function()
{if(this._cf.normal.node){this._cf.normal.node._type="IG_normals";return this._cf.normal.node;}else{return null;}},getNormalTextureURL:function()
{if(this._cf.normal.node){return this._cf.normal.node._vf.url;}else{return null;}},getTexCoordTexture:function()
{if(this._cf.texCoord.node){this._cf.texCoord.node._type="IG_texCoords";return this._cf.texCoord.node;}else{return null;}},getTexCoordTextureURL:function()
{if(this._cf.texCoord.node){return this._cf.texCoord.node._vf.url;}else{return null;}},getColorTexture:function()
{if(this._cf.color.node){this._cf.color.node._type="IG_colors";return this._cf.color.node;}else{return null;}},getColorTextureURL:function()
{if(this._cf.color.node){return this._cf.color.node._vf.url;}else{return null;}},getTextures:function()
{var textures=[];var index=this.getIndexTexture();if(index)textures.push(index);for(i=0;i<this.numCoordinateTextures();i++){var coord=this.getCoordinateTexture(i);if(coord)textures.push(coord);}
var normal=this.getNormalTexture();if(normal)textures.push(normal);var texCoord=this.getTexCoordTexture();if(texCoord)textures.push(texCoord);var color=this.getColorTexture();if(color)textures.push(color);return textures;}}));x3dom.registerNodeType("IndexedFaceSet","Geometry3D",defineClass(x3dom.nodeTypes.X3DComposedGeometryNode,function(ctx){x3dom.nodeTypes.IndexedFaceSet.superClass.call(this,ctx);this.addField_SFFloat(ctx,'creaseAngle',0);this.addField_MFInt32(ctx,'coordIndex',[]);this.addField_MFInt32(ctx,'normalIndex',[]);this.addField_MFInt32(ctx,'colorIndex',[]);this.addField_MFInt32(ctx,'texCoordIndex',[]);this.addField_SFBool(ctx,'convex',true);},{nodeChanged:function()
{var time0=new Date().getTime();this.handleAttribs();var indexes=this._vf.coordIndex;if(indexes.length&&indexes[indexes.length-1]!=-1)
{indexes.push(-1);x3dom.debug.logWarning('Last index value should be -1.');}
var normalInd=this._vf.normalIndex;var texCoordInd=this._vf.texCoordIndex;var colorInd=this._vf.colorIndex;var hasNormal=false,hasNormalInd=false;var hasTexCoord=false,hasTexCoordInd=false;var hasColor=false,hasColorInd=false;var colPerVert=this._vf.colorPerVertex;var normPerVert=this._vf.normalPerVertex;if(normalInd.length>0)
{hasNormalInd=true;}
if(texCoordInd.length>0)
{hasTexCoordInd=true;}
if(colorInd.length>0)
{hasColorInd=true;}
var positions,normals,texCoords,colors;var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);positions=coordNode.getPoints();var normalNode=this._cf.normal.node;if(normalNode)
{hasNormal=true;normals=normalNode._vf.vector;}
else{hasNormal=false;}
var texMode="",numTexComponents=2;var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if(texCoordNode)
{if(texCoordNode._vf.point){hasTexCoord=true;texCoords=texCoordNode._vf.point;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}}
else if(texCoordNode._vf.mode){texMode=texCoordNode._vf.mode;}}
else{hasTexCoord=false;}
this._mesh._numTexComponents=numTexComponents;var numColComponents=3;var colorNode=this._cf.color.node;if(colorNode)
{hasColor=true;colors=colorNode._vf.color;if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
else{hasColor=false;}
this._mesh._numColComponents=numColComponents;this._mesh._indices[0]=[];this._mesh._positions[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._colors[0]=[];var i,j,t,cnt,faceCnt;var p0,p1,p2,n0,n1,n2,t0,t1,t2,c0,c1,c2;if((this._vf.creaseAngle<=x3dom.fields.Eps)||(positions.length>65535)||(hasNormal&&hasNormalInd)||(hasTexCoord&&hasTexCoordInd)||(hasColor&&hasColorInd))
{if(this._vf.creaseAngle<=x3dom.fields.Eps)
x3dom.debug.logWarning('Fallback to inefficient multi-index mode since creaseAngle=0.');if(this._vf.convex){t=0;cnt=0;faceCnt=0;this._mesh._multiIndIndices=[];this._mesh._posSize=positions.length;for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){t=0;faceCnt++;continue;}
if(hasNormalInd){x3dom.debug.assert(normalInd[i]!=-1);}
if(hasTexCoordInd){x3dom.debug.assert(texCoordInd[i]!=-1);}
if(hasColorInd){x3dom.debug.assert(colorInd[i]!=-1);}
switch(t)
{case 0:p0=+indexes[i];if(hasNormalInd&&normPerVert){n0=+normalInd[i];}
else if(hasNormalInd&&!normPerVert){n0=+normalInd[faceCnt];}
else{n0=p0;}
if(hasTexCoordInd){t0=+texCoordInd[i];}
else{t0=p0;}
if(hasColorInd&&colPerVert){c0=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c0=+colorInd[faceCnt];}
else{c0=p0;}
t=1;break;case 1:p1=+indexes[i];if(hasNormalInd&&normPerVert){n1=+normalInd[i];}
else if(hasNormalInd&&!normPerVert){n1=+normalInd[faceCnt];}
else{n1=p1;}
if(hasTexCoordInd){t1=+texCoordInd[i];}
else{t1=p1;}
if(hasColorInd&&colPerVert){c1=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c1=+colorInd[faceCnt];}
else{c1=p1;}
t=2;break;case 2:p2=+indexes[i];if(hasNormalInd&&normPerVert){n2=+normalInd[i];}
else if(hasNormalInd&&!normPerVert){n2=+normalInd[faceCnt];}
else{n2=p2;}
if(hasTexCoordInd){t2=+texCoordInd[i];}
else{t2=p2;}
if(hasColorInd&&colPerVert){c2=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c2=+colorInd[faceCnt];}
else{c2=p2;}
t=3;this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);if(hasNormal){this._mesh._normals[0].push(normals[n0].x);this._mesh._normals[0].push(normals[n0].y);this._mesh._normals[0].push(normals[n0].z);this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);}
else{this._mesh._multiIndIndices.push(p0,p1,p2);}
if(hasColor){this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c0].a);}
this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t0].x);this._mesh._texCoords[0].push(texCoords[t0].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t0].z);}
this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}}
break;case 3:p1=p2;t1=t2;if(normPerVert){n1=n2;}
if(colPerVert){c1=c2;}
p2=+indexes[i];if(hasNormalInd&&normPerVert){n2=+normalInd[i];}else if(hasNormalInd&&!normPerVert){}else{n2=p2;}
if(hasTexCoordInd){t2=+texCoordInd[i];}else{t2=p2;}
if(hasColorInd&&colPerVert){c2=+colorInd[i];}else if(hasColorInd&&!colPerVert){}else{c2=p2;}
this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);if(hasNormal){this._mesh._normals[0].push(normals[n0].x);this._mesh._normals[0].push(normals[n0].y);this._mesh._normals[0].push(normals[n0].z);this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);}
else{this._mesh._multiIndIndices.push(p0,p1,p2);}
if(hasColor){this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c0].a);}
this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t0].x);this._mesh._texCoords[0].push(texCoords[t0].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t0].z);}
this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}}
break;default:}}}
else{var linklist=new x3dom.DoublyLinkedList();var data={};cnt=0;faceCnt=0;for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){var multi_index_data=x3dom.EarClipping.getMultiIndexes(linklist);for(j=0;j<multi_index_data.indices.length;j++)
{this._mesh._indices[0].push(cnt);cnt++;this._mesh._positions[0].push(multi_index_data.point[j].x,multi_index_data.point[j].y,multi_index_data.point[j].z);if(hasNormal){this._mesh._normals[0].push(multi_index_data.normals[j].x,multi_index_data.normals[j].y,multi_index_data.normals[j].z);}
if(hasColor){this._mesh._colors[0].push(multi_index_data.colors[j].r,multi_index_data.colors[j].g,multi_index_data.colors[j].b);if(numColComponents===4){this._mesh._colors[0].push(multi_index_data.colors[j].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(multi_index_data.texCoords[j].x,multi_index_data.texCoords[j].y);if(numTexComponents===3){this._mesh._texCoords[0].push(multi_index_data.texCoords[j].z);}}}
linklist=new x3dom.DoublyLinkedList();faceCnt++;continue;}
if(hasNormal){if(hasNormalInd&&normPerVert){data.normals=normals[normalInd[i]];}else if(hasNormalInd&&!normPerVert){data.normals=normals[normalInd[faceCnt]];}else{data.normals=normals[indexes[i]];}}
if(hasColor){if(hasColorInd&&colPerVert){data.colors=colors[colorInd[i]];}else if(hasColorInd&&!colPerVert){data.colors=colors[colorInd[faceCnt]];}else if(colPerVert){data.colors=colors[indexes[i]];}else{data.colors=colors[faceCnt];}}
if(hasTexCoord){if(hasTexCoordInd){data.texCoords=texCoords[texCoordInd[i]];}else{data.texCoords=texCoords[indexes[i]];}}
linklist.appendNode(new x3dom.DoublyLinkedList.ListNode(positions[indexes[i]],indexes[i],data.normals,data.colors,data.texCoords));}}
if(!hasNormal){this._mesh.calcNormals(this._vf.creaseAngle);}
if(!hasTexCoord){this._mesh.calcTexCoords(texMode);}
this._mesh.splitMesh();}
else
{t=0;if(this._vf.convex){for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){t=0;continue;}
switch(t){case 0:n0=+indexes[i];t=1;break;case 1:n1=+indexes[i];t=2;break;case 2:n2=+indexes[i];t=3;this._mesh._indices[0].push(n0,n1,n2);break;case 3:n1=n2;n2=+indexes[i];this._mesh._indices[0].push(n0,n1,n2);break;}}}else{linklist=new x3dom.DoublyLinkedList();for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){var linklist_indices=x3dom.EarClipping.getIndexes(linklist);for(j=0;j<linklist_indices.length;j++){this._mesh._indices[0].push(linklist_indices[j]);}
linklist=new x3dom.DoublyLinkedList();continue;}
linklist.appendNode(new x3dom.DoublyLinkedList.ListNode(positions[indexes[i]],indexes[i]));}}
this._mesh._positions[0]=positions.toGL();if(hasNormal){this._mesh._normals[0]=normals.toGL();}
else{this._mesh.calcNormals(this._vf.creaseAngle);}
if(hasTexCoord){this._mesh._texCoords[0]=texCoords.toGL();this._mesh._numTexComponents=numTexComponents;}
else{this._mesh.calcTexCoords(texMode);}
if(hasColor){this._mesh._colors[0]=colors.toGL();this._mesh._numColComponents=numColComponents;}}
this._mesh._invalidate=true;this._mesh._numFaces=0;this._mesh._numCoords=0;for(i=0;i<this._mesh._indices.length;i++){this._mesh._numFaces+=this._mesh._indices[i].length/3;this._mesh._numCoords+=this._mesh._positions[i].length/3;}
var time1=new Date().getTime()-time0;},fieldChanged:function(fieldName)
{if(fieldName!="coord"&&fieldName!="normal"&&fieldName!="texCoord"&&fieldName!="color")
{x3dom.debug.logWarning("IndexedFaceSet: fieldChanged for "+
fieldName+" not yet implemented!");return;}
var pnts=this._cf.coord.node._vf.point;var n=pnts.length;var texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if((this._vf.creaseAngle<=x3dom.fields.Eps)||(n>65535)||(this._vf.normalIndex.length>0&&this._cf.normal.node)||(this._vf.texCoordIndex.length>0&&texCoordNode)||(this._vf.colorIndex.length>0&&this._cf.color.node))
{this._mesh._positions[0]=[];this._mesh._indices[0]=[];this._mesh._normals[0]=[];this._mesh._texCoords[0]=[];this._mesh._colors[0]=[];var indexes=this._vf.coordIndex;var normalInd=this._vf.normalIndex;var texCoordInd=this._vf.texCoordIndex;var colorInd=this._vf.colorIndex;var hasNormal=false,hasNormalInd=false;var hasTexCoord=false,hasTexCoordInd=false;var hasColor=false,hasColorInd=false;var colPerVert=this._vf.colorPerVertex;var normPerVert=this._vf.normalPerVertex;if(normalInd.length>0)
{hasNormalInd=true;}
if(texCoordInd.length>0)
{hasTexCoordInd=true;}
if(colorInd.length>0)
{hasColorInd=true;}
var positions,normals,texCoords,colors;var coordNode=this._cf.coord.node;x3dom.debug.assert(coordNode);positions=coordNode.getPoints();var normalNode=this._cf.normal.node;if(normalNode)
{hasNormal=true;normals=normalNode._vf.vector;}
else{hasNormal=false;}
var texMode="",numTexComponents=2;texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
if(texCoordNode)
{if(texCoordNode._vf.point){hasTexCoord=true;texCoords=texCoordNode._vf.point;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.TextureCoordinate3D)){numTexComponents=3;}}
else if(texCoordNode._vf.mode){texMode=texCoordNode._vf.mode;}}
else{hasTexCoord=false;}
this._mesh._numTexComponents=numTexComponents;var numColComponents=3;var colorNode=this._cf.color.node;if(colorNode)
{hasColor=true;colors=colorNode._vf.color;if(x3dom.isa(colorNode,x3dom.nodeTypes.ColorRGBA)){numColComponents=4;}}
else{hasColor=false;}
this._mesh._numColComponents=numColComponents;var i,j,t,cnt,faceCnt;var p0,p1,p2,n0,n1,n2,t0,t1,t2,c0,c1,c2;if(this._vf.convex){t=0;cnt=0;faceCnt=0;this._mesh._multiIndIndices=[];this._mesh._posSize=positions.length;for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){t=0;faceCnt++;continue;}
if(hasNormalInd){x3dom.debug.assert(normalInd[i]!=-1);}
if(hasTexCoordInd){x3dom.debug.assert(texCoordInd[i]!=-1);}
if(hasColorInd){x3dom.debug.assert(colorInd[i]!=-1);}
switch(t)
{case 0:p0=+indexes[i];if(hasNormalInd&&normPerVert){n0=+normalInd[i];}
else if(hasNormalInd&&!normPerVert){n0=+normalInd[faceCnt];}
else{n0=p0;}
if(hasTexCoordInd){t0=+texCoordInd[i];}
else{t0=p0;}
if(hasColorInd&&colPerVert){c0=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c0=+colorInd[faceCnt];}
else{c0=p0;}
t=1;break;case 1:p1=+indexes[i];if(hasNormalInd&&normPerVert){n1=+normalInd[i];}
else if(hasNormalInd&&!normPerVert){n1=+normalInd[faceCnt];}
else{n1=p1;}
if(hasTexCoordInd){t1=+texCoordInd[i];}
else{t1=p1;}
if(hasColorInd&&colPerVert){c1=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c1=+colorInd[faceCnt];}
else{c1=p1;}
t=2;break;case 2:p2=+indexes[i];if(hasNormalInd&&normPerVert){n2=+normalInd[i];}
else if(hasNormalInd&&!normPerVert){n2=+normalInd[faceCnt];}
else{n2=p2;}
if(hasTexCoordInd){t2=+texCoordInd[i];}
else{t2=p2;}
if(hasColorInd&&colPerVert){c2=+colorInd[i];}
else if(hasColorInd&&!colPerVert){c2=+colorInd[faceCnt];}
else{c2=p2;}
t=3;this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);if(hasNormal){this._mesh._normals[0].push(normals[n0].x);this._mesh._normals[0].push(normals[n0].y);this._mesh._normals[0].push(normals[n0].z);this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);}
else{this._mesh._multiIndIndices.push(p0,p1,p2);}
if(hasColor){this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c0].a);}
this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t0].x);this._mesh._texCoords[0].push(texCoords[t0].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t0].z);}
this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}}
break;case 3:p1=p2;t1=t2;if(normPerVert){n1=n2;}
if(colPerVert){c1=c2;}
p2=+indexes[i];if(hasNormalInd&&normPerVert){n2=+normalInd[i];}else if(hasNormalInd&&!normPerVert){}else{n2=p2;}
if(hasTexCoordInd){t2=+texCoordInd[i];}else{t2=p2;}
if(hasColorInd&&colPerVert){c2=+colorInd[i];}else if(hasColorInd&&!colPerVert){}else{c2=p2;}
this._mesh._indices[0].push(cnt++,cnt++,cnt++);this._mesh._positions[0].push(positions[p0].x);this._mesh._positions[0].push(positions[p0].y);this._mesh._positions[0].push(positions[p0].z);this._mesh._positions[0].push(positions[p1].x);this._mesh._positions[0].push(positions[p1].y);this._mesh._positions[0].push(positions[p1].z);this._mesh._positions[0].push(positions[p2].x);this._mesh._positions[0].push(positions[p2].y);this._mesh._positions[0].push(positions[p2].z);if(hasNormal){this._mesh._normals[0].push(normals[n0].x);this._mesh._normals[0].push(normals[n0].y);this._mesh._normals[0].push(normals[n0].z);this._mesh._normals[0].push(normals[n1].x);this._mesh._normals[0].push(normals[n1].y);this._mesh._normals[0].push(normals[n1].z);this._mesh._normals[0].push(normals[n2].x);this._mesh._normals[0].push(normals[n2].y);this._mesh._normals[0].push(normals[n2].z);}
else{this._mesh._multiIndIndices.push(p0,p1,p2);}
if(hasColor){this._mesh._colors[0].push(colors[c0].r);this._mesh._colors[0].push(colors[c0].g);this._mesh._colors[0].push(colors[c0].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c0].a);}
this._mesh._colors[0].push(colors[c1].r);this._mesh._colors[0].push(colors[c1].g);this._mesh._colors[0].push(colors[c1].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c1].a);}
this._mesh._colors[0].push(colors[c2].r);this._mesh._colors[0].push(colors[c2].g);this._mesh._colors[0].push(colors[c2].b);if(numColComponents===4){this._mesh._colors[0].push(colors[c2].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(texCoords[t0].x);this._mesh._texCoords[0].push(texCoords[t0].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t0].z);}
this._mesh._texCoords[0].push(texCoords[t1].x);this._mesh._texCoords[0].push(texCoords[t1].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t1].z);}
this._mesh._texCoords[0].push(texCoords[t2].x);this._mesh._texCoords[0].push(texCoords[t2].y);if(numTexComponents===3){this._mesh._texCoords[0].push(texCoords[t2].z);}}
break;default:}}}
else{var linklist=new x3dom.DoublyLinkedList();var data={};cnt=0;faceCnt=0;for(i=0;i<indexes.length;++i)
{if(indexes[i]==-1){var multi_index_data=x3dom.EarClipping.getMultiIndexes(linklist);for(j=0;j<multi_index_data.indices.length;j++)
{this._mesh._indices[0].push(cnt);cnt++;this._mesh._positions[0].push(multi_index_data.point[j].x,multi_index_data.point[j].y,multi_index_data.point[j].z);if(hasNormal){this._mesh._normals[0].push(multi_index_data.normals[j].x,multi_index_data.normals[j].y,multi_index_data.normals[j].z);}
if(hasColor){this._mesh._colors[0].push(multi_index_data.colors[j].r,multi_index_data.colors[j].g,multi_index_data.colors[j].b);if(numColComponents===4){this._mesh._colors[0].push(multi_index_data.colors[j].a);}}
if(hasTexCoord){this._mesh._texCoords[0].push(multi_index_data.texCoords[j].x,multi_index_data.texCoords[j].y);if(numTexComponents===3){this._mesh._texCoords[0].push(multi_index_data.texCoords[j].z);}}}
linklist=new x3dom.DoublyLinkedList();faceCnt++;continue;}
if(hasNormal){if(hasNormalInd&&normPerVert){data.normals=normals[normalInd[i]];}else if(hasNormalInd&&!normPerVert){data.normals=normals[normalInd[faceCnt]];}else{data.normals=normals[indexes[i]];}}
if(hasColor){if(hasColorInd&&colPerVert){data.colors=colors[colorInd[i]];}else if(hasColorInd&&!colPerVert){data.colors=colors[colorInd[faceCnt]];}else{data.colors=colors[indexes[i]];}}
if(hasTexCoord){if(hasTexCoordInd){data.texCoords=texCoords[texCoordInd[i]];}else{data.texCoords=texCoords[indexes[i]];}}
linklist.appendNode(new x3dom.DoublyLinkedList.ListNode(positions[indexes[i]],indexes[i],data.normals,data.colors,data.texCoords));}}
if(!hasNormal){this._mesh.calcNormals(this._vf.creaseAngle);}
if(!hasTexCoord){this._mesh.calcTexCoords(texMode);}
this._mesh.splitMesh();this._mesh._invalidate=true;this._mesh._numFaces=0;this._mesh._numCoords=0;for(i=0;i<this._mesh._indices.length;i++){this._mesh._numFaces+=this._mesh._indices[i].length/3;this._mesh._numCoords+=this._mesh._positions[i].length/3;}
Array.forEach(this._parentNodes,function(node){node.setGeoDirty();});}
else{if(fieldName=="coord")
{this._mesh._positions[0]=pnts.toGL();this._mesh._invalidate=true;Array.forEach(this._parentNodes,function(node){node._dirty.positions=true;});}
else if(fieldName=="color")
{pnts=this._cf.color.node._vf.color;this._mesh._colors[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.colors=true;});}
else if(fieldName=="normal")
{pnts=this._cf.normal.node._vf.vector;this._mesh._normals[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.normals=true;});}
else if(fieldName=="texCoord")
{texCoordNode=this._cf.texCoord.node;if(x3dom.isa(texCoordNode,x3dom.nodeTypes.MultiTextureCoordinate)){if(texCoordNode._cf.texCoord.nodes.length)
texCoordNode=texCoordNode._cf.texCoord.nodes[0];}
pnts=texCoordNode._vf.point;this._mesh._texCoords[0]=pnts.toGL();Array.forEach(this._parentNodes,function(node){node._dirty.texcoords=true;});}}}}));x3dom.registerNodeType("SphereSegment","Geometry3D",defineClass(x3dom.nodeTypes.X3DSpatialGeometryNode,function(ctx){x3dom.nodeTypes.SphereSegment.superClass.call(this,ctx);this.addField_SFFloat(ctx,'radius',1);this.addField_MFFloat(ctx,'longitude',[]);this.addField_MFFloat(ctx,'latitude',[]);this.addField_SFVec2f(ctx,'stepSize',1,1);var r=this._vf.radius;var longs=this._vf.longitude;var lats=this._vf.latitude;var subx=longs.length,suby=lats.length;var latNumber,longNumber;var latitudeBands=suby;var longitudeBands=subx;var theta,sinTheta,cosTheta;var phi,sinPhi,cosPhi;var x,y,z,u,v;for(latNumber=0;latNumber<=latitudeBands;latNumber++){theta=((lats[latNumber]+90)*Math.PI)/180;sinTheta=Math.sin(theta);cosTheta=Math.cos(theta);for(longNumber=0;longNumber<=longitudeBands;longNumber++){phi=((longs[longNumber])*Math.PI)/180;sinPhi=Math.sin(phi);cosPhi=Math.cos(phi);x=-cosPhi*sinTheta;y=-cosTheta;z=-sinPhi*sinTheta;u=longNumber/(longitudeBands-1);v=latNumber/(latitudeBands-1);this._mesh._positions[0].push(r*x);this._mesh._positions[0].push(r*y);this._mesh._positions[0].push(r*z);this._mesh._normals[0].push(x);this._mesh._normals[0].push(y);this._mesh._normals[0].push(z);this._mesh._texCoords[0].push(u);this._mesh._texCoords[0].push(v);}}
var first,second;for(latNumber=0;latNumber<latitudeBands;latNumber++){for(longNumber=0;longNumber<longitudeBands;longNumber++){first=(latNumber*(longitudeBands+1))+longNumber;second=first+longitudeBands+1;this._mesh._indices[0].push(first);this._mesh._indices[0].push(second);this._mesh._indices[0].push(first+1);this._mesh._indices[0].push(second);this._mesh._indices[0].push(second+1);this._mesh._indices[0].push(first+1);}}
this._mesh._invalidate=true;this._mesh._numFaces=this._mesh._indices[0].length/3;this._mesh._numCoords=this._mesh._positions[0].length/3;},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("X3DTexture3DNode","Texturing3D",defineClass(x3dom.nodeTypes.X3DTextureNode,function(ctx){x3dom.nodeTypes.X3DTexture3DNode.superClass.call(this,ctx);},{}));x3dom.registerNodeType("ComposedTexture3D","Texturing3D",defineClass(x3dom.nodeTypes.X3DTexture3DNode,function(ctx){x3dom.nodeTypes.ComposedTexture3D.superClass.call(this,ctx);this.addField_MFNode('texture',x3dom.nodeTypes.X3DTexture3DNode);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("ImageTexture3D","Texturing3D",defineClass(x3dom.nodeTypes.X3DTexture3DNode,function(ctx){x3dom.nodeTypes.ImageTexture3D.superClass.call(this,ctx);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("PixelTexture3D","Texturing3D",defineClass(x3dom.nodeTypes.X3DTexture3DNode,function(ctx){x3dom.nodeTypes.PixelTexture3D.superClass.call(this,ctx);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("TextureCoordinate3D","Texturing3D",defineClass(x3dom.nodeTypes.X3DTextureCoordinateNode,function(ctx){x3dom.nodeTypes.TextureCoordinate3D.superClass.call(this,ctx);this.addField_MFVec3f(ctx,'point',[]);}));x3dom.registerNodeType("TextureTransform3D","Texturing3D",defineClass(x3dom.nodeTypes.X3DTextureTransformNode,function(ctx){x3dom.nodeTypes.TextureTransform3D.superClass.call(this,ctx);this.addField_SFVec3f(ctx,'center',0,0,0);this.addField_SFRotation(ctx,'rotation',0,0,1,0);this.addField_SFVec3f(ctx,'scale',1,1,1);this.addField_SFVec3f(ctx,'translation',0,0,0);this.addField_SFRotation(ctx,'scaleOrientation',0,0,1,0);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("TextureTransformMatrix3D","Texturing3D",defineClass(x3dom.nodeTypes.X3DTextureTransformNode,function(ctx){x3dom.nodeTypes.TextureTransformMatrix3D.superClass.call(this,ctx);this.addField_SFMatrix4f(ctx,'matrix',1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.registerNodeType("ImageTextureAtlas","Texturing",defineClass(x3dom.nodeTypes.Texture,function(ctx){x3dom.nodeTypes.ImageTexture.superClass.call(this,ctx);this.addField_SFInt32(ctx,'numberOfSlices',0);this.addField_SFInt32(ctx,'slicesOverX',0);this.addField_SFInt32(ctx,'slicesOverY',0);},{nodeChanged:function(){},fieldChanged:function(fieldName){}}));x3dom.versionInfo={version:'1.5.0-dev',revision:'2e6fa03fee431113695e1bacbe9a0a9552e6cbbf',date:'Fri Apr 12 11:43:52 2013 +0200'};