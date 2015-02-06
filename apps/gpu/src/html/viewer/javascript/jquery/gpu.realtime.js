var options;
var extraParams;

var previewResultsURL = "";
var creatorOutput = "";
var generateImage = false;
var concatenateFiles = false;

var maxParams = 20;

var width = 512;
var height = 512;
var frames = 360 / 2;
var framesX = 9;


function getFile(elementId){
  document.getElementById(elementId).click();
}

function sub(obj, idOfDisplay){
  var file = obj.value;
  var tmp = file.split("\\");
  var fileName = tmp[tmp.length-1];
  document.getElementById(idOfDisplay).value = fileName;
  
  previewResultsURL = "";
}

function getJobID() {
  var text = "width=" + width + "&height=" + height + "&frames=" + frames + "&framesX=" + framesX + "&script=" + editor.getValue();
  return md5(text);
}

function initScript() {
  spin('preview');
  
  // Set additional data not part of the form
  extraParams = {
    'jobID':   getJobID(),
    'script':  editor.getValue(),
    'width':   width,
    'height':  height,
    'view':    matrixToQueryString(viewMatrix)
  };

  var url = "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage?" + $.param(extraParams);
  console.log(url);
  var request = $.ajax({
    type: "POST",
    url: url,
  })
  
  request.done(function( data ) {
//    console.log("request done url: " + url);
    var imageViewer = document.getElementById("render");
    imageViewer.setAttribute("src", url);
    unspin();
  });
 
  request.fail(function( jqXHR, textStatus ) {
    alert( "Request failed: " + textStatus );
    unspin();
  });
}

function rotateModel(event) {
  if (!mouseDown) return;
//console.log("start: " + dragStart.x + " " + dragStart.y);
//console.log("end:   " + event.clientX + " " + event.clientY);

  var dx = Math.abs(event.clientX - dragStart.x);
  var dy = Math.abs(event.clientY - dragStart.y);

  // Skip if not enough drag
  if (dx + dy < 10) return;
  
  setViewMatrix(dx, dy);
  var matrixStr = matrixToQueryString(viewMatrix);
//  console.log(matrixStr);

  extraParams = {
    'jobID':  getJobID(),
    'view':   matrixToQueryString(viewMatrix)
  };
  
  if (loading) return;
  
//  dragStart.x = event.clientX;
//  dragStart.y = event.clientY;
  
  var imageViewer = document.getElementById("render");
  loading = true;

  var url = "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage?" + $.param(extraParams);
  imageViewer.setAttribute("src", url);
  
}

/*
function getRender() {
  //spin('preview');
  
  // Set additional data not part of the form
  extraParams = {
    'jobID':   getJobID(),
    'script':  editor.getValue(),
    'width':   width,
    'height':  height,
    'frames':  frames,
    'framesX': framesX
  };
//  console.log(editor.getValue());
//  console.log($.param(extraParams));
  
  var url = "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage?" + $.param(extraParams);
  showSpriteImage(url,frames,framesX);
  
///////////////////////////////////////////////
  var request = $.ajax({
    type: "GET",
    url: "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage",
  })
  
  request.done(function( data ) {
    console.log(data);
    showSpriteImage(data);
  });
 
  request.fail(function( jqXHR, textStatus ) {
    alert( "Request failed: " + textStatus );
  });

///////////////////////////////////////////////
  options = {
    url: "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage",
    type: "get",
    contentType: "image/png",
    success: generateRenderResponse,
    error: function(xhr, textStatus, errorThrown) {
      alert("Status: " + textStatus + ",\nError: " + errorThrown);
      console.log("Status: " + textStatus + ", error: " + errorThrown);
      unspin();
    }
  };

  jQuery('#form').ajaxSubmit(options);

}

function postRender() {

  // Set additional data not part of the form
  extraParams = {
    'jobID':   getJobID(),
    'width':   width,
    'height':  height,
    'frames':  frames,
    'framesX': framesX
  };

  options = {
    url: "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage",
    type: "post",
    dataType: 'json',
    timeout: 180000,
    beforeSubmit: jsonForm,
    success: generateRenderResponse,
    error: function(xhr, textStatus, errorThrown) {
      alert("Status: " + textStatus + ",\nError: " + errorThrown);
      console.log("Status: " + textStatus + ", error: " + errorThrown);
      unspin();
    }
  };

  jQuery('#form').ajaxSubmit(options);
}

function generateRenderResponse(data) {
  showSpriteImage("http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage?width=512&height=512&frames=36&frameX=6");
  console.log(data);
//  $("#sprite-image").attr("src", data);
}
*/
////////////////////////////////////////////////////////

function loadFile(method, url, data, type) {
  jQuery.ajax({
    url: url,
    data: data,
    type: method,
    async: false,
    dataType: type,
    timeout:  30000,
    success: function(response) {
      editor.setValue(response);
      return true;
    },
    error: function(response) {
      return false;
    }
  });
}

/////////////////////////////////////////
// Other things
/////////////////////////////////////////

function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

function gup( name ) {
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}

function isWebGLSupported() {
  var forceImage = gup('forceImage');

  if (forceImage == 'true') {
    return false;
  }

  var cvs = document.createElement('canvas');
  var contextNames = ["webgl","experimental-webgl","moz-webgl","webkit-3d"];
  var ctx;

  if ( navigator.userAgent.indexOf("MSIE") >= 0 ) {
    try {
      ctx = WebGLHelper.CreateGLContext(cvs, 'canvas');
    } catch(e) {}
  } else {
    for ( var i = 0; i < contextNames.length; i++ ) {
      try {
        ctx = cvs.getContext(contextNames[i]);
        if ( ctx ) break;
      } catch(e){}
    }
  }

  if ( ctx ) return true;
  return false;
}

function spin(process) {
  if (process == null || process == 'preview') {
    jQuery('#preview-button').hide();
    jQuery('#loading').show();
  } else if (process == 'printability') {
    jQuery('#printability-button').hide();
    jQuery('#checking').show();
  } else if (process == 'save') {
    jQuery('#save-button').hide();
    jQuery('#saving').show();
  } else if (process == 'upload') {
    jQuery('#upload-button').hide();
    jQuery('#uploading').show();
  }
//  jQuery('#save-cover').show();
//  jQuery('#save-button').css('color', '#777777');
}

function unspin() {
  jQuery("#loading").hide();
  jQuery("#preview-button").show();
  jQuery("#checking").hide();
  jQuery("#printability-button").show();
  jQuery("#saving").hide();
  jQuery("#save-button").show();
  jQuery("#uploading").hide();
  jQuery("#upload-button").show();
}

/** Checks if a element value starts with http:// */
function isUriParam(id) {
  var str = "http://";
  var val = document.getElementById(id).value;

  if (val.substring(0, str.length) === str) {
    return true;
  } else {
    return false;
  }
}

/**
 * Get the value of a url query string param
 *
 * @param name The name of the param to get
 */
$.extend({
  getUrlVars: function () {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for (var i = 0; i < hashes.length; i++) {
      hash = hashes[i].split('=');
      vars.push(hash[0]);
      vars[hash[0]] = hash[1];
    }
    return vars;
  },
  getUrlVar: function (name) {
    return $.getUrlVars()[name];
  }
});

/** Print the node and its children for debug purposes */
function debug(element) {
  var serialized;

  try {
    // XMLSerializer exists in current Mozilla browsers
    serializer = new XMLSerializer();
    serialized = serializer.serializeToString(element);
  }
  catch (e) {
    // Internet Explorer has a different approach to serializing XML
    serialized = element.xml;
  }

  alert(serialized);
}

function setViewMatrix(dx, dy) {
  var angle = Math.sqrt(dx*dx + dy*dy); // / distanceToCenter;
  console.log("angle: " + angle);
  var rot = new Array(); 
  rot[0] = dy;
  rot[1] = dx;
  rot[2] = 0;
  normalize(rot);
  console.log(rot);
  var axis = $V([rot[0], rot[1], rot[2]]);
  
  var matrix3f = Matrix.Rotation(angle, axis);
  console.log("*** matrix3f");
  console.log(matrixToQueryString(matrix3f));
  
  // indexes start at 1
  var matrix4f = $M([
    [ matrix3f.e(1,1), matrix3f.e(1,2), matrix3f.e(1,3), 0],
    [ matrix3f.e(2,1), matrix3f.e(2,2), matrix3f.e(2,3), 0],
    [ matrix3f.e(3,1), matrix3f.e(3,2), matrix3f.e(3,3), 0],
    [ 0, 0, 0, 1 ]
  ]);
  console.log("*** matrix4f");
  console.log(matrixToQueryString(matrix4f));
  viewMatrix = viewMatrix.multiply(matrix4f);
}

function setViewMatrix2(dx, dy) {
  var aaRot = toAxisAngle(dx, dy);
  var matrix = axisAngleToMatrix(aaRot);
  
  viewMatrix = viewMatrix.multiply(matrix);
}

function axisAngleToMatrix(aaRot) {
  var rx = aaRot[0];
  var ry = aaRot[1];
  var rz = aaRot[2];
  var rangle = aaRot[3];
  
  // matrix as Sylvester matrix object
  var matrix = $M([
    [Math.cos(rangle) + rx*rx*(1-Math.cos(rangle)), rx*ry*(1-Math.cos(rangle)) - rz*Math.sin(rangle), rx*rz*(1-Math.cos(rangle)) + ry*Math.sin(rangle), 0],
    [ry*rx*(1-Math.cos(rangle)) + rz*Math.sin(rangle), Math.cos(rangle) + ry*ry*(1-Math.cos(rangle)), ry*rz*(1-Math.cos(rangle)) - rx*Math.sin(rangle), 0],
    [rz*rx*(1-Math.cos(rangle)) - ry*Math.sin(rangle), rz*ry*(1-Math.cos(rangle)) + rx*Math.sin(rangle), Math.cos(rangle) + rz*rz*(1-Math.cos(rangle)), 0],
    [0, 0, 0, 1]
  ]);
  
  return matrix;
}

function toAxisAngle(dx, dy) {
  var angle = Math.sqrt(dx*dx + dy*dy) / 20;	
  var rot = new Array(); 
  rot[0] = dy;
  rot[1] = dx;
  rot[2] = 0;
  rot[3] = angle;
  normalize(rot);
  return rot;
}

// normalizes vector
function normalize(p){
  var s = p[0]*p[0]+p[1]*p[1]+p[2]*p[2];
  if(s != 0.0){
    s = Math.sqrt(s);
    p[0] = p[0]/s;
    p[1] = p[1]/s;
    p[2] = p[2]/s;
  }
}

function matrixToQueryString(matrix) {
  var rows = matrix.rows();
  var cols = matrix.cols();
  var str = "";
  
  for (var row=1; row<=rows; row++) {
    for (var col=1; col<=cols; col++) {
      str += matrix.e(row,col) + ", ";
    }
  }
  
  // remove last 2 chars (comma and space)
  return str.substr(0, str.length-2);
}