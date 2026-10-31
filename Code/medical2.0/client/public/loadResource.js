// Giữ kích thước Element UI ở mức mini (đã thiết lập trong index.html)
const size = 'mini'

const loadLink = function(url, rel, type, callback) {
  var link = document.createElement("link")
  var fn = callback || function(){}
  // IE
  if(link.readyState){
    link.onreadystatechange = function(){
      if( link.readyState == 'loaded' || link.readyState == 'complete' ){
        link.onreadystatechange = null;
        fn();
      }
    }
  }else{
    // Trình duyệt khác
    link.onload = function(){
      fn();
    }
  }

  link.setAttribute("rel", rel)
  link.setAttribute("type", type)
  link.setAttribute("href", url)
  document.getElementsByTagName('head')[0].appendChild(link)
  return link
}

const loadJS = function( url, callback) {
  var script = document.createElement('script')
  var fn = callback || function(){}
  // IE
  if(script.readyState){
    script.onreadystatechange = function(){
      if( script.readyState == 'loaded' || script.readyState == 'complete' ) {
        script.onreadystatechange = null
        fn()
      }
    };
  }else{
    // Trình duyệt khác
    script.onload = function() {
      fn()
    }
  }
  script.type = 'text/javascript'
  script.src = url
  document.getElementsByTagName('head')[0].appendChild(script)
  return script
}

const sleep = function(time) {
  return new Promise((resolve) => setTimeout(resolve, time))
}

/* Tải báo cáo bất đồng bộ
  Nạp trước các tài nguyên bất đồng bộ mà dataviewer sử dụng; sau khi chắc chắn
  các tài nguyên này đã nạp xong mới nạp dataview
*/
const loadDataViewer = function() {
  let prevCount = 4 // Số tài nguyên cần nạp trước
  sleep(50).then(() => {
    // echarts
    loadJS('lib/echarts/echarts@5.3.0.js', function(){
      prevCount = prevCount - 1
    })
    // umy-ui
    loadJS('lib/umy-ui/umy-ui@1.1.6.js', function(){
      Vue.prototype.$ELEMENT.size = size
      prevCount = prevCount - 1
    })

    // jspdf
    loadJS('lib/jspdf@2.5.1/jspdf.umd.min.js', function(){
      prevCount = prevCount - 1
    })

    // @antv/g2plot
    loadJS('lib/@antv/g2plot@2.4.8/g2plot.min.js', function(){
      prevCount = prevCount - 1
    })
  })

  const doLoadDataViewer = function() {
    if(prevCount <= 0) {
      sleep(50).then(() => {
        loadLink('lib/vue-dataviewer-1.6.4/vue-dataviewer.css', 'stylesheet', 'text/css', function(){
        })
        loadJS('lib/vue-dataviewer-1.6.4/vue-dataviewer.umd.min.js', function(){
          Vue.prototype.$ELEMENT.size = size
        })
      })
      return
    }

    setTimeout(function() {
      doLoadDataViewer()
    }, 50)
  }
  doLoadDataViewer()
}

function loadSource() {
  loadDataViewer()
}

if (window.addEventListener){
  window.addEventListener("load", loadSource, false);
}else if (window.attachEvent){
  window.attachEvent("onload", loadSource);
}else {
  window.onload = loadSource;
}
