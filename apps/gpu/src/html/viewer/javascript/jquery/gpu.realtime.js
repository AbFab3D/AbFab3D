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

var skipCount = 15;

var rotX = 0;
var rotY = 0;
var zoom = -4;


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
    'rotX':    rotX,  // x rotation in radians
    'rotY':    rotY,  // y rotation in radians
    'zoom':    zoom,  // zoom level (translation in z direction)
//    'view':    matrixToQueryString(viewMatrix)
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

function zoomModel() {
  extraParams = {
    'jobID':  getJobID(),
    'rotX':    rotX,  // x rotation in radians
    'rotY':    rotY,  // y rotation in radians
    'zoom':    zoom  // zoom level (translation in z direction)
  };
  
  if (loading) return;
  
  var imageViewer = document.getElementById("render");

  if (loading) {
    skipCount--;
    if (skipCount > 0) {
      return;
    } else {
      console.log("Skipped too long, reseting");
      skipCount = 15;
    }
  }

  var url = "http://localhost:8080/creator/shapejsRT_v1.0.0/makeImage?" + $.param(extraParams);
  imageViewer.setAttribute("src", url);
}

function rotateModel(dx, dy, radX, radY) {
  if (radX !== undefined && radX !== null && radY !== undefined && radY !== null) {
    rotX += radX;
    rotY += radY;
  } else {
    if (!mouseDown) return;

    rotX += dy / 20;
    rotY += dx / 20;
  }

console.log("rot: " + rotX + " " + rotY);
  
  extraParams = {
    'jobID':  getJobID(),
    'rotX':    rotX,  // x rotation in radians
    'rotY':    rotY,  // y rotation in radians
    'zoom':    zoom  // zoom level (translation in z direction)
  };

  if (loading) {
    skipCount--;
    if (skipCount > 0) {
      return;
    } else {
      console.log("Skipped too long, reseting");
      skipCount = 15;
    }
  }
  
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

function setViewMatrix(rotX, rotY) {
  var trans = $M([
    [1, 0, 0, 0],
    [0, 1, 0, 0],
    [0, 0, 1, -4],
    [0, 0, 0, 1]
  ]);  
  //console.log("trans matrix");
  //console.log(matrixToQueryString(trans));  
  var rxmat = Matrix.RotationX(rotX);
  var rymat = Matrix.RotationY(rotY);  // indexes start at 1
  var rxmat4 = $M([
    [ rxmat.e(1,1), rxmat.e(1,2), rxmat.e(1,3), 0],
    [ rxmat.e(2,1), rxmat.e(2,2), rxmat.e(2,3), 0],
    [ rxmat.e(3,1), rxmat.e(3,2), rxmat.e(3,3), 0],
    [ 0, 0, 0, 1 ]
  ]);
  var rymat4 = $M([
    [ rymat.e(1,1), rymat.e(1,2), rymat.e(1,3), 0],
    [ rymat.e(2,1), rymat.e(2,2), rymat.e(2,3), 0],
    [ rymat.e(3,1), rymat.e(3,2), rymat.e(3,3), 0],
    [ 0, 0, 0, 1 ]
  ]);
  viewMatrix = viewMatrix.multiply(rxmat4).multiply(rymat4);
//  viewMatrix = trans.multiply(rxmat4);
  //console.log("*** final");
  //console.log(matrixToQueryString(viewMatrix));
} 

function round(val, digits) {
  var sigDigits = "1";
  
  for (var i=0; i<digits; i++) {
    sigDigits = sigDigits + "0";
  }

  var dig = parseFloat(sigDigits);
  return Math.round(val * dig) / dig;
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