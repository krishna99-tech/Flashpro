package com.example.flash.ui

import android.view.View
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb

@Composable
fun ThreeJsView(modifier: Modifier = Modifier.fillMaxSize()) {
    val primaryColor = String.format("#%06X", 0xFFFFFF and MaterialTheme.colorScheme.primary.toArgb())
    val secondaryColor = String.format("#%06X", 0xFFFFFF and MaterialTheme.colorScheme.secondary.toArgb())
    
    val html = remember(primaryColor, secondaryColor) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { width: 100%; height: 100%; margin: 0; overflow: hidden; background: transparent; }
                canvas { width: 100%; height: 100%; display: block; }
            </style>
        </head>
        <body>
            <script>
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                document.body.appendChild(canvas);
                function resize() {
                    canvas.width = window.innerWidth;
                    canvas.height = window.innerHeight;
                }
                resize();
                window.addEventListener('resize', resize);
                const points = Array.from({length: 220}, () => ({
                    x: (Math.random() - 0.5) * 800,
                    y: (Math.random() - 0.5) * 800,
                    z: Math.random() * 2 + 0.2
                }));
                let t = 0;
                function frame() {
                    t += 0.01;
                    ctx.clearRect(0, 0, canvas.width, canvas.height);
                    const cx = canvas.width / 2;
                    const cy = canvas.height / 2;
                    points.forEach((p, i) => {
                        const a = t + i * 0.02;
                        const x = Math.cos(a) * p.x * 0.15 - Math.sin(a) * p.y * 0.15;
                        const y = Math.sin(a) * p.x * 0.15 + Math.cos(a) * p.y * 0.15;
                        const s = p.z * 2.5;
                        ctx.fillStyle = '$primaryColor';
                        ctx.globalAlpha = 0.15 + ((i % 10) / 20);
                        ctx.beginPath();
                        ctx.arc(cx + x, cy + y, s, 0, Math.PI * 2);
                        ctx.fill();
                    });
                    ctx.globalAlpha = 0.25;
                    ctx.strokeStyle = '$secondaryColor';
                    ctx.beginPath();
                    ctx.arc(cx, cy, Math.min(canvas.width, canvas.height) * 0.22, 0, Math.PI * 2);
                    ctx.stroke();
                    requestAnimationFrame(frame);
                }
                frame();
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                setBackgroundColor(0x00000000)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
            }
        },
        update = { view ->
            if (view.tag != html) {
                view.tag = html
                view.loadDataWithBaseURL("https://flash.local/", html, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}
