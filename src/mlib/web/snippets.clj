
(ns mlib.web.snippets)

; (defn identicon [u s]
;     (str "http://www.gravatar.com/avatar/" (digest/md5 (str u)) "?d=identicon&s=" s))


(def one-pix-src
  "data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACwAAAAAAQABAAACAkQBADs=")

(def one-pix-img
  [:img {:src one-pix-src}])
;

(defn analytics [id]
  (str
    "\n<script>"
    "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){"
    "(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),"
    "m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)"
    "})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');"
    "ga('create','" id "','auto');"
    "ga('send','pageview');"
    "</script>\n"))
;

(defn yandex-metrika [id]

;  (str
;    "\n<!-- Yandex.Metrika counter -->\n"
;    "<script type='text/javascript'>"
;    "(function(d, w, c){"
;    "  (w[c] = w[c] || []).push(function(){"
;    "    try{ w.yaCounter" id "= new Ya.Metrika({id:" id ","
;    "      webvisor:true, clickmap:true, trackLinks:true,"
;    "      accurateTrackBounce:true, trackHash:true"
;    "    })} catch(e) {};"
;    "    var n = d.getElementsByTagName('script')[0],"
;    "    s = d.createElement('script'),"
;    "    f = function(){ n.parentNode.insertBefore(s, n)};"
;    "    s.type = 'text/javascript';"
;    "    s.async = true;"
;    "    s.src = (d.location.protocol == 'https:'? 'https:':'http:')"
;    "          +'//mc.yandex.ru/metrika/watch.js';"
;    "    if(w.opera == '[object Opera]'){"
;    "      d.addEventListener('DOMContentLoaded', f, false);"
;    "    } else { f(); };"
;    "  });"
;    "})(document, window, 'yandex_metrika_callbacks');</script>"
;    "<noscript><div><img src='//mc.yandex.ru/watch/" id "' alt=''"
;    "  style='position:absolute; left:-9999px;' />"
;    "</div></noscript>"))
;
  (str
    "\n<script type='text/javascript'>"
    "(function (d, w, c) {"
    "   (w[c] = w[c] || []).push(function() {"
    "       try { w.yaCounter" id " = new Ya.Metrika2({ id:" id ","
    "               clickmap:true,"
    "               trackLinks:true,"
    "               accurateTrackBounce:true,"
    "               webvisor:true,"
    "               trackHash:true"
    "           });"
    "       } catch(e) { }"
    "   });"
    "   var n = d.getElementsByTagName('script')[0],"
    "       s = d.createElement('script'),"
    "       f = function () { n.parentNode.insertBefore(s, n); };"
    "   s.type = 'text/javascript';"
    "   s.async = true;"
    "   s.src = 'https://mc.yandex.ru/metrika/watch.js';"
    "   if (w.opera == '[object Opera]') {"
    "       d.addEventListener('DOMContentLoaded', f, false);"
    "   } else { f(); }"
    "})(document, window, 'yandex_metrika_callbacks2');"
    "</script>"
    "<noscript><div><img src='https://mc.yandex.ru/watch/" id "'"
    " style='position:absolute; left:-9999px;' alt='' /></div></noscript>\n"))
;


(defn mailru-top [id]
  (str "
<!-- Rating@Mail.ru counter -->
<script type='text/javascript'>
var _tmr = _tmr || []; _tmr.push({id:'" id "',type:'pageView',start:(new Date()).getTime()});(
function(d, w){ var ts = d.createElement('script'); ts.type = 'text/javascript'; ts.async = true;
ts.src = (d.location.protocol == 'https:'? 'https:': 'http:')+'//top-fwz1.mail.ru/js/code.js';
var f = function(){ var s = d.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ts, s); };
if(w.opera == '[object Opera]'){d.addEventListener('DOMContentLoaded',f,false);} else {f();};
})(document, window);
</script><noscript><div style='position:absolute;left:-10000px;'>
<img src='//top-fwz1.mail.ru/counter?id=" id ";js=na'
  style='border:0;' height='1' width='1' alt='Рейтинг@Mail.ru' /></div></noscript>
"))
;

(defn ya-direct [div_id client stat type limit]
  (str "
<div id='" div_id "'></div>
<script type='text/javascript'>
(function(w, d, n, s, t) {}
    w[n] = w[n] || [];
    w[n].push(function() {})
        Ya.Direct.insertInto(" client ", '" div_id "', {})
            stat_id: " stat ",
            ad_format: 'direct',
            font_size: 1,
            type: '" type "',
            border_type: 'collapse',
            limit: " limit ",
            title_font_size: 3,
            border_radius: true,
            links_underline: false,
            site_bg_color: 'FFFFFF',
            border_color: 'FBE5C0',
            title_color: '0000CC',
            url_color: '006600',
            text_color: '000000',
            hover_color: '0066FF',
            sitelinks_color: '0000CC',
            favicon: true,
            no_sitelinks: false
        ;
    ;
    t = d.getElementsByTagName('script')[0];
    s = d.createElement('script');
    s.src = '//an.yandex.ru/system/context.js';
    s.type = 'text/javascript';
    s.async = true;
    t.parentNode.insertBefore(s, t));
(window, document, 'yandex_context_callbacks');
</script>
"))

(defn ya-rtb [blk horizontal?]
  (str "
<div id='yandex_rtb_" blk "' class='yandex-adaptive'></div>
<script type='text/javascript'>
  (function(w, d, n, s, t){
    w[n] = w[n] || [];
    w[n].push( function(){
      Ya.Context.AdvManager.render({
        blockId: '" blk "',
        renderTo: 'yandex_rtb_" blk "',
        horizontalAlign: " horizontal? ",
        async: true
      });
    });
    t = d.getElementsByTagName('script')[0];
    s = d.createElement('script');
    s.type = 'text/javascript';
    s.src = '//an.yandex.ru/system/context.js';
    s.async = true;
    t.parentNode.insertBefore(s, t);
  })(this, this.document, 'yandexContextAsyncCallbacks');
</script>
"))

;;.
