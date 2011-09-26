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
    document.getElementById("tangram").submit();;
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
