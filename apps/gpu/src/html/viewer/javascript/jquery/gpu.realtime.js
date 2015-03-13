var options;
var extraParams;

var previewResultsURL = "";
var creatorOutput = "";
var generateImage = false;

var maxParams = 20;

var width = 512;
var height = 512;
var frames = 360 / 2;
var framesX = 9;
var imgType = 'JPG';

var skipCount = 15;

var rotX = 0;
var rotY = 0;
var curRotation = [0,1,0,0];
var zoom = -4;
var quality = 0.5;
var viewChanged = false;
var forceFull = false;  //  should we force a full render
var highQuality = false;

var maxFPS = 30;  // maximum frame rate to shoot for
var updatingScene = false;
var usSkipCount = 15;

var scriptErrorLines = [];

(function() {
  var lastTime = 0;
  var vendors = ['ms', 'moz', 'webkit', 'o'];
  for(var x = 0; x < vendors.length && !window.requestAnimationFrame; ++x) {
    window.requestAnimationFrame = window[vendors[x]+'RequestAnimationFrame'];
    window.cancelAnimationFrame = window[vendors[x]+'CancelAnimationFrame']
    || window[vendors[x]+'CancelRequestAnimationFrame'];
  }

  if (!window.requestAnimationFrame)
    window.requestAnimationFrame = function(callback, element) {
      var currTime = new Date().getTime();
      var timeToCall = Math.max(0, 16 - (currTime - lastTime));
      var id = window.setTimeout(function() { callback(currTime + timeToCall); },
          timeToCall);
      lastTime = currTime + timeToCall;
      return id;
    };

  if (!window.cancelAnimationFrame)
    window.cancelAnimationFrame = function(id) {
      clearTimeout(id);
    };
}());

var frameNum = 0;
// function for updating the image
function draw() {
  setTimeout(function() {
    window.requestAnimationFrame(draw);

    if (!viewChanged) return;

    viewChanged = false;
    frameNum++;

    var useQuality = quality;
    var imgType = "jpg";
    
    if (highQuality) {
      useQuality = 1.0;
      imgType = "png";
      highQuality = false;
    }

    if (forceFull) {
      extraParams = {
        'jobID': getJobID(),
        'script': editor.getValue(),
        'width': width,
        'height': height,
        'axisAngle': curRotationToQueryString(),
        'zoom': zoom.toFixed(4),  // zoom level (translation in z direction)
        'imgType': imgType,
        'quality': useQuality,
        'frameNum': frameNum     // cache busting logic
      };
    } else {
      extraParams = {
        'jobID': getJobID(),
        'axisAngle': curRotationToQueryString(),
        'zoom': zoom.toFixed(4),  // zoom level (translation in z direction)
        'imgType': imgType,
        'quality': useQuality,
        'frameNum': frameNum     // cache busting logic
      };
    }

    if (loading) {
      skipCount--;
      if (skipCount > 0) {
        return;
      } else {
        console.log("Skipped too long, resetting");
      }
    }
    skipCount = 15;

    var imageViewer = document.getElementById("render");
    loading = true;

    var url;
    if (forceFull) {
      url = "/creator/shapejsRT_v1.0.0/makeImage?" + $.param(extraParams);
      if (paramData !== undefined && paramData !== null) {
        url = url + "&" + $.param(paramDataToQueryString(paramData));
      }
      forceFull = false;
    } else {
      url = "/creator/shapejsRT_v1.0.0/makeImageCached?" + $.param(extraParams);
    }

    imageViewer.setAttribute("src", url);

  }, 1000 / maxFPS);
}

// Start the drawing loop
draw();


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
//  var text = "width=" + width + "&height=" + height + "&frames=" + frames + "&framesX=" + framesX + "&script=" + editor.getValue();
  var text = editor.getValue();

  // TODO: jobID does not need params anymore
/*
  $.each( paramData, function( key, value ) {
    text += "&" + key +"=" + value;
  });
*/
  return userID + "_" + md5(text);
}

function paramDataToQueryString(params) {
  var newParams = {};
  $.each(params, function(key, val) {
    newParams["shapeJS_" + key] = val;
  });
  return newParams;
}

function updateScene() {
  if (updatingScene) {
    //console.log("Still loading, skipping");
    usSkipCount--;
    if (usSkipCount > 0) {
      return;
    } else {
      console.log("Skipped too long, resetting");
    }
  }

  clearScriptHighlights();
  updatingScene = true;
  usSkipCount = 15;

  // Set additional data not part of the form
  extraParams = {
    'jobID':   getJobID(),
    'script':  editor.getValue()
  };

  options = {
    url: "/creator/shapejsRT_v1.0.0/updateScene",
    type: "post",
    timeout: 180000,
    beforeSubmit: prepForm,
    success: function(data) {
      updatingScene = false;
      showLogs(data);
      unspin();
      if (data.success) {
        viewChanged = true;  // force a redraw
        deltaParams.length = 0;
      } else {
        $("#render").attr("src", "");
      }
    },
    error: function(xhr, textStatus, errorThrown) {
      updatingScene = false;
      alert( "REQUEST FAILED\n\nCode: " + xhr.status + "\nError: " + xhr.statusText );
      console.log(xhr);
      $("#render").attr("src", "");
      unspin();
    }
  };

  jQuery('#form').ajaxSubmit(options);
}

/** Save the current scene */
function saveScene() {
  extraParams = {
    'jobID':   getJobID(),
  };

  var fileURL = "/creator/shapejsRT_v1.0.0/saveSceneCached?" + $.param(extraParams);

  // for non-IE
  if (!window.ActiveXObject) {
    var save = document.createElement('a');
    save.href = fileURL;
    save.target = '_blank';
    save.download = fileURL;
    var evt = document.createEvent('MouseEvents');
    evt.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
    save.dispatchEvent(evt);
    (window.URL || window.webkitURL).revokeObjectURL(save.href);
  }

  // for IE
  else if ( !! window.ActiveXObject && document.execCommand)     {
    var _window = window.open(fileURL, "_blank");
    _window.document.close();
    _window.document.execCommand('SaveAs', true, fileURL)
    _window.close();
  }
}

function zoomModel() {
  viewChanged = true;
}

function rotateModel(dx, dy, radX, radY) {
  if (radX !== undefined && radX !== null && radY !== undefined && radY !== null) {
    addRotation(radY, radX);
  } else {
    if (!mouseDown) return;

    addRotation(dx / 120, dy / 120);
  }

  viewChanged = true;
}

// Remove default right button menu
window.oncontextmenu = function ()
{
  return false;     // cancel default menu
}

function pickModel(e, element) {
  var pos = getClickPosition(e, element);
  
//  console.log("Pick: " + pos[0] + " " + pos[1]);

  extraParams = {
    'x': pos[0],
    'y': pos[1],
    'jobID':   getJobID(),
//    'script':  editor.getValue(),
    'width':   width,
    'height':  height,
    'axisAngle': curRotationToQueryString(),
    'zoom':    zoom.toFixed(4)  // zoom level (translation in z direction)
  };

  var url = "/creator/shapejsRT_v1.0.0/pickCached?" + $.param(extraParams);

/*
  if (paramData !== undefined && paramData !== null) {
    url = url + "&" + $.param(paramDataToQueryString(paramData));
  }
*/
  var request = $.ajax({
    type: "POST",
    url: url
  })

  request.done(function( data ) {
    // Response with normals (-10000, -10000, -10000) means no valid position on geometry was clicked
    // TODO: Clicking in the bounding box of model returns valid response, but not a valid geometry position.
    //       Indicated with normals (0,0,0). Should fix on server side.
    if ( (data.normal[0] == -10000 && data.normal[1] == -10000 && data.normal[2] == -10000) ||
         (data.normal[0] == 0 && data.normal[1] == 0 && data.normal[2] == 0) )
         return;
         
    $(pickDataContainer).val(data["pos"] + "," + data["normal"]).change();
  });

  request.fail(function( jqXHR, textStatus ) {
    alert( "REQUEST FAILED\n\nCode: " + xhr.status + "\nError: " + xhr.statusText );

    // TODO: use pick with complete script
  });
}

function getClickPosition(event, element) {
  // Positioning in html page is (0,0) at upper left, while we need (0,0) at lower left
  // Convert by subtracing y position from height var
  var offset = $("#render").offset();
  var x = Math.round(event.pageX - offset.left);
  var y = Math.round(height - (event.pageY - offset.top));
  
  if (x < 0) {
    x = 0;
  } else if (x > width) {
    x = width;
  }
  if (y < 0) {
    y = 0;
  } else if (y > height) {
    y = height;
  }

  return [x,y];
}

function setQuality(q) {
  quality = q;
  viewChanged = true;
}

function renderHighQuality() {
  highQuality = true;
  viewChanged = true;
}

function showLogs(obj) {
  $("#logger").empty();

  var display = "";
  
  var val = obj.evalTime;
  var val2 = obj.opCount;
  var val3 = obj.opSize;
  var val4 = obj.dataSize;
  if (val !== null && val2 !== null && val3 != null && val4 != null) {
    $( "<span class='logType'>Evaluation time:</span><span class='log-info'>" + val + "</span><span class='logType'> ms.</span><span class='logType'>   opCount:</span><span class='log-info'>" + val2 + "</span><span class='logType'> opSize:</span><span class='log-info'>" + val3 + "</span><span class='logType'> dataSize:</span><span class='log-info'>" + val4 + "</span><p style='line-height:50%'>&nbsp;</p>" ).appendTo( "#logger" );
  }

  val = obj.printLog;
  if (val !== undefined && val !== null && val.length > 0) {
    $( "<p class='log-type'>Prints:</p>" ).appendTo( "#logger" );
    
    var prints = val.split("\n");
    $.each(prints, function(i, text) {
      $( "<p class='log-info'>" + text + "</p>" ).appendTo( "#logger" );
    });
  }
  
  val = obj.errorLog;
  if (val !== undefined && val !== null && val.length > 0) {
    $( "<p class='log-type'>Errors:</p>" ).appendTo( "#logger" );
    
    var prints = val.split("\n");
    $.each(prints, function(i, text) {
      $( "<p class='log-info'>" + text + "</p>" ).appendTo( "#logger" );
      highlightScriptError(text);
    });
  }
}

function highlightScriptError(text) {
  var marker = "Script Line(";
  var index = text.indexOf(marker);

  if (index >= 0) {
    var start = index + marker.length;
    var end = text.indexOf(")", start);
    var lineNumber = text.substring(index + marker.length, end);
    editor.removeLineClass(lineNumber-1, 'background', 'CodeMirror-activeline-background');
    editor.addLineClass(lineNumber-1, 'background', 'CodeMirror-error-line-bg');
    scriptErrorLines.push(lineNumber-1);
  }
}

function clearScriptHighlights() {
  $.each(scriptErrorLines, function(i, val) {
    editor.removeLineClass(val, 'background', 'CodeMirror-error-line-bg');
  });
  
  scriptErrorLines.length = 0;
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

function spin(process) {
  if (process == null || process == 'preview') {
    jQuery('#loading').show();
  } else if (process == 'save') {
    jQuery('#save-button').hide();
    jQuery('#saving').show();
  } else if (process == 'upload') {
    jQuery('#upload-button').hide();
    jQuery('#uploading').show();
  }
}

function unspin() {
  jQuery("#loading").hide();
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

function toAxisAngle(dx,dy){
  var angle = Math.sqrt(dx*dx + dy*dy);	
  var rot = new Array(); 
  rot[0] = dy;
  rot[1] = dx;
  rot[2] = 0;
  rot[3] = angle;
  normalize(rot);
  return rot;
}

/** Add dx and dy to current rotaton in axis angle */
function addRotation(dx,dy) {
  var newRot = toAxisAngle(dx,dy);

  to_quaternion(newRot);
  to_quaternion(curRotation);
  
  var totalRot = new Array();
  multiply_quaternion(curRotation, newRot, totalRot);
  from_quaternion(totalRot);
  
  curRotation = [totalRot[0], totalRot[1], totalRot[2], totalRot[3]];
}

/** Convert quaternion to (axis, angle) **/
function from_quaternion( q ) {
  var angle = Math.acos(q[3]) * 2.0;
  var s = Math.sin(angle / 2.0);
  if(s == 0.0) {
    s = 1;
    angle = 0.0;
  }
  q[0] /= s;
  q[1] /= s; 
  q[2] /= s;
  q[3]  = angle;
}

/** Convert (rotation, angle) to quaternion */
function to_quaternion( r ){
  var s = Math.sin(r[3]/2.0);
  r[0] *= s; r[1] *= s; r[2] *= s; r[3] = Math.cos(r[3] / 2.0);
}

/** Multiply two quaternions */
function multiply_quaternion(q1, q2, result) {
  result[0] = q2[3] * q1[0] + q2[0] * q1[3] +	q2[1] * q1[2] - q2[2] * q1[1];
  result[1] = q2[3] * q1[1] + q2[1] * q1[3] + q2[2] * q1[0] - q2[0] * q1[2];
  result[2] = q2[3] * q1[2] + q2[2] * q1[3] + q2[0] * q1[1] - q2[1] * q1[0];
  result[3] = q2[3] * q1[3] - q2[0] * q1[0] - q2[1] * q1[1] - q2[2] * q1[2];
}

/** Normalizes vector part of rotation */
function normalize(p) {
  var s = p[0]*p[0]+p[1]*p[1]+p[2]*p[2];
  if(s != 0.0) {
    s = Math.sqrt(s);
    p[0] = p[0]/s;
    p[1] = p[1]/s;
    p[2] = p[2]/s;
  }
}

function toRotationMatrix(dx,dy) {
  var axisAngle = toAxisAngle(dx,dy);
  var rx = axisAngle[0];
  var ry = axisAngle[1];
  var rz = axisAngle[2];
  var rangle = axisAngle[3];
  
  var matrix = $M([
    [Math.cos(rangle) + rx*rx*(1-Math.cos(rangle)), rx*ry*(1-Math.cos(rangle)) - rz*Math.sin(rangle), rx*rz*(1-Math.cos(rangle)) + ry*Math.sin(rangle), 0],
    [ry*rx*(1-Math.cos(rangle)) + rz*Math.sin(rangle), Math.cos(rangle) + ry*ry*(1-Math.cos(rangle)), ry*rz*(1-Math.cos(rangle)) - rx*Math.sin(rangle), 0],
    [rz*rx*(1-Math.cos(rangle)) - ry*Math.sin(rangle), rz*ry*(1-Math.cos(rangle)) + rx*Math.sin(rangle), Math.cos(rangle) + rz*rz*(1-Math.cos(rangle)), 0],
    [0, 0, 0, 1]
  ]);
  
  return matrix;
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

function curRotationToQueryString() {
  return curRotation[0] + "," + curRotation[1] + "," + curRotation[2] + "," + curRotation[3];
}

function nl2br(text){
  return text.replace(/(\r\n|\n\r|\r|\n)/g, "<br/>");
};