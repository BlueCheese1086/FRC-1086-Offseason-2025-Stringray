package frc.robot.util;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    public WebServer(int port) throws Exception {
        super(port);
        start(SOCKET_READ_TIMEOUT, false);
        System.out.println("Web server started on port " + port);
    }

    @Override
    public Response serve(IHTTPSession session) {

        int circleSize = 70;
        int radius = 230;
        int count = 12;
        int containerSize = 650;
        int center = containerSize / 2;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial; background:#111; color:white; text-align:center; }")
                .append(".hex-container { width:")
                .append(containerSize)
                .append("px; height:")
                .append(containerSize)
                .append("px; margin:40px auto; position:relative; background:#222; border-radius:12px; }")
                .append(".circle { width:")
                .append(circleSize)
                .append("px; height:")
                .append(circleSize)
                .append("px; border-radius:50%; background:#9d4bff; position:absolute; cursor:pointer; transition:0.2s; }")
                .append(".circle.selected { background:#3aff6f; }") // green when level selected
                .append(".popup { display:none; position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:black; padding:20px 30px; border-radius:10px; border:2px solid white; }")
                .append(".popup button { margin:6px; padding:8px 15px; font-size:16px; cursor:pointer; border-radius:6px; }")
                .append("</style>")

                .append("<script>")
                .append("let activeCircle = null;")
                .append("function circleClicked(id) {")
                .append("  activeCircle = id;")
                .append("  document.getElementById('popup-title').innerText = 'Circle ' + id;")
                .append("  document.getElementById('popup-result').innerText = '';")
                .append("  document.getElementById('popup').style.display = 'block';")
                .append("}")
                // Updated: selectLevel now marks the circle green
                .append("function selectLevel(level) {")
                .append("  if (activeCircle !== null) {")
                .append("    const circle = document.getElementById('circle-' + activeCircle);")
                .append("    circle.classList.add('selected');") // turn circle green
                .append("    circle.dataset.level = level;") // optional: store selected level
                .append("  }")
                .append("  document.getElementById('popup-result').innerText = level + ' selected';")
                .append("}")
                .append("function closePopup() { document.getElementById('popup').style.display = 'none'; }")
                .append("</script>")

                .append("</head><body>")
                .append("<h1>Robot Data</h1>")
                .append("<div class='hex-container'>");

        double rotationOffset = Math.toRadians(15); // rotate circle ring 15 degrees

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count + rotationOffset;

            int x = (int) (center + radius * Math.cos(angle) - circleSize / 2);
            int y = (int) (center + radius * Math.sin(angle) - circleSize / 2);

            html.append("<div id='circle-")
                    .append(i)
                    .append("' class='circle' style='left:")
                    .append(x)
                    .append("px; top:")
                    .append(y)
                    .append("px;' onclick='circleClicked(")
                    .append(i)
                    .append(")'></div>");
        }

        html.append("</div>")
                .append("<div id='popup' class='popup'>")
                .append("  <h2 id='popup-title'></h2>")
                .append("  <p id='popup-result'></p>")
                .append("  <button onclick=\"selectLevel('L4')\">L4</button>")
                .append("  <button onclick=\"selectLevel('L3')\">L3</button>")
                .append("  <button onclick=\"selectLevel('L2')\">L2</button>")
                .append("  <button onclick=\"selectLevel('L1')\">L1</button><br><br>")
                .append("  <button onclick='closePopup()'>Close</button>")
                .append("</div>")
                .append("</body></html>");

        return newFixedLengthResponse(html.toString());
    }

}
