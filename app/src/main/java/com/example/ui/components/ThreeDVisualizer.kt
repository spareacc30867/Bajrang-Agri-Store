package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.Product
import com.example.model.Mesh3D
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeDVisualizer(
    product: Product,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Media mode selection: "video" or "image"
    var selectedMediaMode by remember { mutableStateOf("video") }
    
    // Dialog state for custom link editing
    var showEditLinkDialog by remember { mutableStateOf(false) }
    
    // Read/Write saved custom video URLs using SharedPreferences
    val sharedPrefs: SharedPreferences = remember {
        context.getSharedPreferences("jyoti_custom_videos", Context.MODE_PRIVATE)
    }
    
    // Dynamic video URL state
    var currentVideoUrl by remember(product.id) {
        mutableStateOf(sharedPrefs.getString("video_url_${product.id}", product.defaultVideoUrl) ?: product.defaultVideoUrl)
    }
    
    var tempUrlInput by remember { mutableStateOf(currentVideoUrl) }
    
    // When selected product changes, reset temp url input and default to video mode
    LaunchedEffect(product.id) {
        currentVideoUrl = sharedPrefs.getString("video_url_${product.id}", product.defaultVideoUrl) ?: product.defaultVideoUrl
        tempUrlInput = currentVideoUrl
        selectedMediaMode = "video"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header stats & badge info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE FIELD TEST SHOWCASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgroOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = AgroDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Top External Action links
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Open in Youtube Button
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentVideoUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open video link.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(AgroLightBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Watch on YouTube",
                            tint = AgroDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Custom URL Configuration Button
                    IconButton(
                        onClick = { showEditLinkDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(AgroLightBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Video Link",
                            tint = AgroOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector Switches (Video Demo vs. HD Image Frame)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AgroLightBg)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("Play Field Video Demo", "video", Icons.Default.PlayArrow),
                    Triple("HD Implement Photo", "image", Icons.Default.Image)
                ).forEach { (label, mode, icon) ->
                    val isActive = selectedMediaMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) AgroDark else Color.Transparent)
                            .clickable { selectedMediaMode = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isActive) Color.White else AgroSlate,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (isActive) Color.White else AgroDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Media Container Display Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
            ) {
                if (selectedMediaMode == "image") {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Blurred Backdrop
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alpha = 0.22f,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Clear Foreground Product
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = "HD Image View of ${product.name}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "JYOTI SPECIAL QUALITY",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Play YouTube Embedded Video
                    YouTubePlayerWebView(
                        videoUrl = currentVideoUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tech Spec summary Box below the media player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AgroLightBg)
                    .border(1.dp, AgroSilver, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Implement Info",
                        tint = AgroOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Field Operations Overview:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (product.id) {
                                "disc_harrow" -> "The heavy dynamic disc set cuts deep clods, chops organic root residue, and prepares standard levels in clay fields. Tap on 'Edit' to change default working field URLs."
                                "rotary_tiller" -> "Using bidirectional Forward & Reverse rotators, this machine simplifies soil shredding tasks across orchards and moist paddy fields with perfect torque distribution."
                                else -> "Inspired by premium pulverization grades, it ensures fine single-pass seedbeds. Fully customizable and fits perfectly with Cat-I & Cat-II standard multi-speed PTO connections."
                            },
                            fontSize = 11.sp,
                            color = AgroSlate,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Dynamic Edit Video Link Dialog
    if (showEditLinkDialog) {
        AlertDialog(
            onDismissRequest = { showEditLinkDialog = false },
            title = {
                Text(
                    text = "Configure Video URL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AgroDark
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Paste a custom YouTube field demo URL below. The app will automatically parse the ID and stream the video directly in the live dashboard above!",
                        fontSize = 12.sp,
                        color = AgroSlate,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = tempUrlInput,
                        onValueChange = { tempUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("YouTube Video URL") },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgroGreen,
                            focusedLabelColor = AgroGreen
                        )
                    )

                    Button(
                        onClick = {
                            tempUrlInput = product.defaultVideoUrl
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroLightBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Default Video Link", color = AgroDark, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedInput = tempUrlInput.trim()
                        if (trimmedInput.isNotEmpty()) {
                            sharedPrefs.edit().putString("video_url_${product.id}", trimmedInput).apply()
                            currentVideoUrl = trimmedInput
                            Toast.makeText(context, "Custom video link saved and loaded successfully!", Toast.LENGTH_SHORT).show()
                            showEditLinkDialog = false
                        } else {
                            Toast.makeText(context, "URL cannot be empty.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreen)
                ) {
                    Text("Save & Persist", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditLinkDialog = false }
                ) {
                    Text("Cancel", color = AgroSlate)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun YouTubePlayerWebView(videoUrl: String, modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    val videoId = remember(videoUrl) { extractYouTubeVideoId(videoUrl) }

    val htmlContent = remember(videoId, videoUrl) {
        if (videoId != null) {
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    body, html {
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                        background-color: black;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        overflow: hidden;
                    }
                    .iframe-container {
                        position: relative;
                        width: 100%;
                        height: 100%;
                    }
                    iframe {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        border: none;
                    }
                </style>
            </head>
            <body>
                <div class="iframe-container">
                    <iframe 
                        src="https://www.youtube.com/embed/$videoId?autoplay=1&mute=1&playlist=$videoId&loop=1&playsinline=1&enablejsapi=1" 
                        allow="autoplay; encrypted-media; picture-in-picture" 
                        allowfullscreen>
                    </iframe>
                </div>
            </body>
            </html>
            """.trimIndent()
        } else {
            ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    settings.apply {
                        javaScriptEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    tag = videoUrl
                    if (videoId != null) {
                        loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)
                    } else {
                        loadUrl(videoUrl)
                    }
                }
            },
            update = { webView ->
                val lastUrl = webView.tag as? String
                if (lastUrl != videoUrl) {
                    webView.tag = videoUrl
                    isLoading = true
                    if (videoId != null) {
                        webView.loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)
                    } else {
                        webView.loadUrl(videoUrl)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = AgroOrange,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

private fun getYouTubeEmbedUrl(url: String): String {
    val trimmed = url.trim()
    val videoId = extractYouTubeVideoId(trimmed)
    return if (videoId != null) {
        "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1&playlist=$videoId&loop=1&playsinline=1"
    } else {
        // Fallback to loading the URL directly if not a YouTube ID
        trimmed
    }
}

private fun extractYouTubeVideoId(url: String): String? {
    val cleanUrl = url.trim()
    return try {
        when {
            cleanUrl.contains("youtu.be/") -> {
                cleanUrl.substringAfter("youtu.be/").substringBefore("?").substringBefore("/")
            }
            cleanUrl.contains("youtube.com/embed/") -> {
                cleanUrl.substringAfter("youtube.com/embed/").substringBefore("?").substringBefore("/")
            }
            cleanUrl.contains("v=") -> {
                cleanUrl.substringAfter("v=").substringBefore("&").substringBefore("/")
            }
            cleanUrl.contains("youtube.com/shorts/") -> {
                cleanUrl.substringAfter("youtube.com/shorts/").substringBefore("?").substringBefore("/")
            }
            else -> {
                // If the coordinate input is direct video ID
                if (cleanUrl.length in 10..12 && !cleanUrl.contains("/") && !cleanUrl.contains(".")) {
                    cleanUrl
                } else {
                    null
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun Interactive3DView(
    mesh: Mesh3D,
    modifier: Modifier = Modifier
) {
    var angleX by remember { mutableStateOf(0.4f) }
    var angleY by remember { mutableStateOf(0.6f) }
    
    Box(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    angleY += dragAmount.x * 0.01f
                    angleX -= dragAmount.y * 0.01f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2
            val cy = height / 2
            
            // Rotate each vertex based on touch drag coordinates
            val rotatedVertices = mesh.vertices.map { v ->
                v.rotateY(angleY).rotateX(angleX)
            }
            
            // Dynamic projection perspective formula
            val d = 400f
            val distanceOffset = 220f
            
            // Render depth index sorting (Painters Algorithm)
            val sortedFaces = mesh.faces.sortedByDescending { it.getAverageZ(rotatedVertices) }
            
            sortedFaces.forEach { face ->
                val path = Path()
                var first = true
                var isValid = true
                
                face.vertexIndices.forEach { idx ->
                    if (idx in rotatedVertices.indices) {
                        val vertex = rotatedVertices[idx]
                        val projectedZ = vertex.z + distanceOffset
                        if (projectedZ <= 10f) {
                            isValid = false
                            return@forEach
                        }
                        
                        val scale = d / projectedZ
                        val px = cx + vertex.x * scale
                        val py = cy + vertex.y * scale
                        
                        if (first) {
                            path.moveTo(px, py)
                            first = false
                        } else {
                            path.lineTo(px, py)
                        }
                    } else {
                        isValid = false
                    }
                }
                
                if (isValid && !first) {
                    path.close()
                    if (face.isOutlineOnly) {
                        drawPath(
                            path = path,
                            color = face.color,
                            style = Stroke(width = 3f)
                        )
                    } else {
                        drawPath(
                            path = path,
                            color = face.color
                        )
                        // Add a thin black border around face for stunning volumetric depth separation
                        drawPath(
                            path = path,
                            color = Color.Black.copy(alpha = 0.25f),
                            style = Stroke(width = 1f)
                        )
                    }
                }
            }
        }
        
        // Dynamic touch control coaching tip overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "◀ Drag finger to rotate 3D workbench model ▶",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
