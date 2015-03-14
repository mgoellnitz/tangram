var keystate = false;

function keydown(ev) {
  if (!ev) {
    return true;
  } // if

  if (!ev.ctrlKey) {
    return true;
  } // if

  var keyCode = ev.keyCode;

  if (keyCode == 17) {
    return false;
  } // if
  if (keyCode == 83) {
    document.getElementById("tangram").submit();
    keystate = true;
    return false;
  } // if

  return true;
} // keydown()

function keypress(ev) {
  var keyCode = ev.keyCode;
  if (keystate) {
    ev.returnValue = false;
    keystate = false;
  } // if

  return true;
} // keypress()

function getWindowWidth() {
  var result = 0;
  if (typeof (window.innerWidth) == 'number') {
    // Non-IE
    result = window.innerWidth;
  } else {
    if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
      // IE 6+ in 'standards compliant mode'
      result = document.documentElement.clientWidth;
    } else {
      if (document.body && (document.body.clientWidth || document.body.clientHeight)) {
        // IE 4 compatible
        result = document.body.clientWidth;
      }
    }
  }
  return result;
}