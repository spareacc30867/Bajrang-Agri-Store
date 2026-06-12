package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.Product
import com.example.model.ProductData
import com.example.model.Mesh3D
import com.example.ui.components.ThreeDVisualizer
import com.example.ui.theme.*
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Inquiry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Screen State
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var selectedProductFor3DWorkshop by remember { mutableStateOf(ProductData.products[2]) } // Default to Rotavator in 3D workshop

    // Inquiry form states
    var inquiryName by remember { mutableStateOf("") }
    var inquiryMobile by remember { mutableStateOf("") }
    var inquiryProductSelection by remember { mutableStateOf(ProductData.products[2].name) }
    var inquiryMessage by remember { mutableStateOf("") }
    var isSubmittingInquiry by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AgroLightBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Scrollable Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // 1. STICKY / TOP BRANDING BAR
                TopNavbar()

                // 2. HERO LANDING SECTION
                HeroSection(
                    onExploreProductsClick = {
                        coroutineScope.launch {
                            // Smooth scroll down to Product Section
                            scrollState.animateScrollTo(750)
                        }
                    },
                    onInquireClick = {
                        coroutineScope.launch {
                            // Smooth scroll down to Contact inquiry form
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                )

                // 3. STATS CHANGER & MOTIVATIONAL LABELS
                StatisticsQuickLookGrid()

                Spacer(modifier = Modifier.height(24.dp))

                // 4. INTERACTIVE 3D WORKSHOP SECTION (MANDATORY REQUIREMENT)
                InteractiveWorkshopBanner()
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column {
                        // Product toggle selectors for 3D view
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, AgroSilver, RoundedCornerShape(16.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ProductData.products.forEach { prod ->
                                val isSelected = selectedProductFor3DWorkshop.id == prod.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AgroGreen else Color.Transparent)
                                        .clickable { selectedProductFor3DWorkshop = prod }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prod.name,
                                        color = if (isSelected) Color.White else AgroDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full interactive Field Live Video Showroom & HD Demo with subtle entry animations
                        AnimatedContent(
                            targetState = selectedProductFor3DWorkshop,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(350)) + 
                                        slideInVertically(
                                            initialOffsetY = { 40 }, 
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
                                        ) togetherWith
                                fadeOut(animationSpec = tween(200))
                            },
                            label = "product_section_change"
                        ) { targetProduct ->
                            ThreeDVisualizer(product = targetProduct)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 5. PRODUCTS GRID SECTION (Double visual representation)
                ProductsShowcaseSection(
                    onProductClick = { prod ->
                        selectedProductForDetail = prod
                    },
                    onInquireProduct = { prod ->
                        inquiryProductSelection = prod.name
                        inquiryMessage = "Hello, I am interested in seeking a specialized model quote for your '${prod.name}' machinery implement. Please provide pricing options."
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 6. WHY CHOOSE US
                WhyChooseUsSection()

                Spacer(modifier = Modifier.height(32.dp))

                // 7. MULTI-GRID GALLERY SECTION
                GallerySection()

                Spacer(modifier = Modifier.height(32.dp))

                // 8. CUSTOMER TESTIMONIALS (Indian Farmers reviews)
                TestimonialsSection()

                Spacer(modifier = Modifier.height(32.dp))

                // 9. MAP AND GPS INTEGRATION
                AgriculturalOperationsIndicator()

                Spacer(modifier = Modifier.height(32.dp))

                // 10. CONTACT INFO & FORM
                ContactSection(
                    context = context,
                    inquiryName = inquiryName,
                    onNameChanged = { inquiryName = it },
                    inquiryMobile = inquiryMobile,
                    onMobileChanged = { inquiryMobile = it },
                    productSelection = inquiryProductSelection,
                    onProductSelectionChanged = { inquiryProductSelection = it },
                    inquiryMessage = inquiryMessage,
                    onMessageChanged = { inquiryMessage = it },
                    isSubmitting = isSubmittingInquiry,
                    onFormSubmit = {
                        if (inquiryName.isBlank() || inquiryMobile.isBlank()) {
                            Toast.makeText(context, "Please fill in your Name and Mobile Number.", Toast.LENGTH_LONG).show()
                        } else {
                            isSubmittingInquiry = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(800) // Brief feedback
                                isSubmittingInquiry = false
                                
                                val formattedMsg = """
                                    *NEW QUOTE REQUEST (BAJRANG AGRI IMPLEMENTS)*
                                    👤 Name: $inquiryName
                                    📱 Client Mobile: $inquiryMobile
                                    ⚙️ Product: $inquiryProductSelection
                                    ✉️ Message: ${if (inquiryMessage.isNotBlank()) inquiryMessage else "No extra details specified."}
                                """.trimIndent()

                                try {
                                    val uriText = Uri.encode(formattedMsg)
                                    val waUrl = "https://wa.me/918980030865?text=$uriText"
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(waUrl)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Redirecting to WhatsApp to send quote details...", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    try {
                                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:8980030865")).apply {
                                            putExtra("sms_body", formattedMsg)
                                        }
                                        context.startActivity(smsIntent)
                                        Toast.makeText(context, "Opening SMS app to submit details to owner...", Toast.LENGTH_LONG).show()
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "Your details are ready: $inquiryName ($inquiryMobile). Please contact Bhavesh Solanki at 8980030865.", Toast.LENGTH_LONG).show()
                                    }
                                }

                                inquiryName = ""
                                inquiryMobile = ""
                                inquiryMessage = ""
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 11. FOOTER
                FooterSection(
                    onHomeClick = { coroutineScope.launch { scrollState.animateScrollTo(0) } },
                    onProductsClick = { coroutineScope.launch { scrollState.animateScrollTo(750) } }
                )
            }

            // FLOATING ACTION ALERTS (Floating WhatsApp Widget + Call Panel)
            FloatingActionHub(
                context = context,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )

            // FULLSCREEN DETAIL DIALOG (When user clicks Know More)
            selectedProductForDetail?.let { product ->
                ProductDetailDialog(
                    product = product,
                    onDismiss = { selectedProductForDetail = null },
                    onInquireClick = {
                        inquiryProductSelection = product.name
                        inquiryMessage = "Hello, I want to inquire about the technical specifications and pricing details for the '${product.name}'. Please contact me via phone."
                        selectedProductForDetail = null
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                )
            }
        }
    }
}

// --------------------------------------------------------------------
// UI COMPONENTS DEFINED BELOW
// --------------------------------------------------------------------

@Composable
fun TopNavbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, AgroSilver)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // High-fidelity dynamic brand logo image
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_jyoti_logo),
                contentDescription = "Jyoti Logo",
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, AgroSilver, RoundedCornerShape(10.dp))
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )

            Column {
                Text(
                    text = "JYOTI",
                    style = MaterialTheme.typography.titleLarge,
                    color = AgroDark,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "BAJRANG AGRI IMPLEMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AgroOrange,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Vijay Solanki
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AgroGreenLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Text(
                        text = "VIJAY SOLANKI",
                        color = AgroGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bhavesh Solanki
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AgroGreenLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Text(
                        text = "BHAVESH SOLANKI",
                        color = AgroGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    onExploreProductsClick: () -> Unit,
    onInquireClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF334155), Color(0xFF0F172A)),
                    center = Offset(300f, 150f),
                    radius = 800f
                )
            )
    ) {
        // Decorative Abstract Field Parallax Gradients
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.2f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AgroGreen, Color.Transparent),
                        startY = 0f,
                        endY = 600f
                    )
                )
        )

        // Geometric overlay tines
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = AgroOrange.copy(alpha = 0.15f),
                radius = 120.dp.toPx(),
                center = Offset(size.width * 0.95f, size.height * 0.8f)
            )
            drawCircle(
                color = AgroGreen.copy(alpha = 0.15f),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.05f, size.height * 0.1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Futuristic Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x33FF6B00))
                    .border(1.dp, AgroOrange.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Quality",
                    tint = AgroOrange,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AN INDUSTRIAL LEADER IN GUJARAT",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Display Typography
            Text(
                text = "Powering Modern\nFarming",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 38.sp,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Premium Agricultural Implements by Bajrang Agri Implements. Master-engineered for rugged tasks, extreme durability, and peak efficiency.",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dual tactile responsive actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onExploreProductsClick,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("explore_products_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AgroOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Explore Products", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Arrow", modifier = Modifier.size(14.dp))
                }

                OutlinedButton(
                    onClick = onInquireClick,
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Contact Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatisticsQuickLookGrid() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val stats = listOf(
            Triple("15k+", "FARMERS ENGAGED", AgroGreen),
            Triple("100%", "DURABLE STEEL", AgroOrange),
            Triple("24/7", "SUPPORT HUB", AgroDark)
        )

        stats.forEach { (main, label, badgeColor) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = main,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgroDark
                    )
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        color = AgroSlate,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveWorkshopBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEAEFEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Live Demo Symbol",
                    tint = AgroGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "JYOTI LIVE FIELD DEMO SHOWROOM",
                style = MaterialTheme.typography.labelMedium,
                color = AgroSlate,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
        Text(
            text = "Watch Working Demonstration Videos in the Fields Live",
            color = AgroDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )
    }
}

@Composable
fun ProductsShowcaseSection(
    onProductClick: (Product) -> Unit,
    onInquireProduct: (Product) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FEATURED HEAVY IMPLEMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AgroOrange,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Our High-End Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    color = AgroDark,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid listing for all premium product cards with Dropbox images
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProductData.products.forEach { prod ->
                ProductCard(
                    product = prod,
                    onClick = { onProductClick(prod) },
                    onInquire = { onInquireProduct(prod) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onInquire: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color.White, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column {
                // Hero image frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Background industrial gradient mesh
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9))
                                )
                            )
                    )

                    // High fidelity Dropbox online image loaded seamlessly via Coil
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Floating glassmorphism technology tags
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xB3000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE DEMO",
                            color = AgroOrange,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Description block
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = AgroDark
                    )

                    Text(
                        text = product.tagline,
                        fontSize = 11.sp,
                        color = AgroOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = product.shortDescription,
                        fontSize = 11.sp,
                        color = AgroSlate,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("know_more_${product.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = AgroDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Know More", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onInquire,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AgroOrange),
                            border = BorderStroke(1.dp, AgroOrange.copy(alpha = 0.4f))
                        ) {
                            Text("Inquire Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhyChooseUsSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "WHY BJARANG AGRI",
            style = MaterialTheme.typography.labelSmall,
            color = AgroOrange,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Uncompromised Quality Core",
            style = MaterialTheme.typography.titleLarge,
            color = AgroDark,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val points = listOf(
            Triple(Icons.Filled.ThumbUp, "High Quality Implements", "We source hardened carbon steels with double sub-arc protective welding."),
            Triple(Icons.Filled.CheckCircle, "Durable Materials", "Tested to operate perfectly across dry, humid, sand, or rigid rocky soils."),
            Triple(Icons.Filled.AccountCircle, "Trusted by Farmers", "Over 15,000 active Indian farmers operate our tools daily in complete safety."),
            Triple(Icons.Filled.Build, "Modern Technology", "Precision simulated machinery layouts using pure digital lab standards."),
            Triple(Icons.Filled.Star, "Affordable Pricing", "Reasonable initial capital investments paired with incredible product lifetime."),
            Triple(Icons.Filled.Call, "Fast Customer Support", "Direct line access to owners: Vijay Solanki & Bhavesh Solanki.")
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(points) { (icon, title, desc) ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(170.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AgroGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = AgroGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroDark,
                            lineHeight = 15.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = desc,
                            fontSize = 10.sp,
                            color = AgroSlate,
                            lineHeight = 13.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GallerySection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "DIGITAL MACHINERY PHOTO GALLERY",
            style = MaterialTheme.typography.labelSmall,
            color = AgroOrange,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Modern Fields & Iron Works",
            style = MaterialTheme.typography.titleLarge,
            color = AgroDark,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // We can draw beautiful simulated abstract cards that looks extremely high-end!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GalleryCardSimulated("Iron Works Assembly", "TIG-welding structural frame testing", AgroGreen, 130.dp)
                GalleryCardSimulated("Plowing Test run", "Perfect soil pulverization in Anand district", AgroOrange, 180.dp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GalleryCardSimulated("Gold Standard Rotator", "Assembling multi speed gear system", AgroDark, 170.dp)
                GalleryCardSimulated("Delivering Trust", "Vijay Solanki & Bhavesh Solanki shipping premium tools", AgroSlate, 140.dp)
            }
        }
    }
}

@Composable
fun GalleryCardSimulated(
    title: String,
    desc: String,
    dominantColor: Color,
    heightDp: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(dominantColor, AgroDark),
                    center = Offset(20f, 20f)
                )
            )
            .clickable { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = desc,
                color = Color.LightGray,
                fontSize = 8.5.sp,
                lineHeight = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TestimonialsSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "TRUSTED AT THE GROUND ROOT",
            style = MaterialTheme.typography.labelSmall,
            color = AgroOrange,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Farmer Success Stories",
            style = MaterialTheme.typography.titleLarge,
            color = AgroDark,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        val reviews = listOf(
            Triple("RameshBhai Patel", "Mehsana, GJ", "I purchased the Bajrang Rotavator with dual trailing boards last season. Tilling wet clay fields became dynamic. Excellent results in single run!"),
            Triple("Bharat Prajapati", "Himmatnagar, GJ", "The Reverse Forward Rotary has marvelous tilling precision. Solid speed gearing is highly stable. Highly recommend contacting Vijay Solanki & Bhavesh Solanki!"),
            Triple("Sanjay Gowda", "Junagadh, GJ", "My Disc Harrow has plowed over 250 acres stone filled drylands. Hardened discs show absolutely zero deformation. Highly durable!")
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews) { (author, location, review) ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(145.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Star Indicator",
                                        tint = AgroOrange,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"$review\"",
                                fontSize = 11.sp,
                                color = AgroSlate,
                                lineHeight = 14.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = author,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgroDark
                            )
                            Text(
                                text = location,
                                fontSize = 9.sp,
                                color = AgroGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgriculturalOperationsIndicator() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = AgroDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "JYOTI & BAJRANG CO-LOCATION MAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgroOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Live Factory & Showroom Setup",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF22C55E))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE PREVIEW", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Maps WebView Wrapper
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                var isMapLoading by remember { mutableStateOf(true) }
                val targetMapUrl = "https://maps.google.com/maps?q=Bajrang%20Agri%20Implements&hl=en&z=15&output=embed"

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isMapLoading = false
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (url.startsWith("http://") || url.startsWith("https://")) {
                                        // Keep output=embed if redirecting inside map view
                                        if (url.contains("maps.google") && !url.contains("output=embed")) {
                                            view?.loadUrl(url + (if (url.contains("?")) "&" else "?") + "output=embed")
                                            return true
                                        }
                                        return false
                                    }
                                    // Launch safe hardware intents for external deep links safely without crashing the WebView or Applet
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        ctx.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    return true
                                }
                            }
                            webChromeClient = android.webkit.WebChromeClient()
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }
                            loadUrl(targetMapUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isMapLoading) {
                    CircularProgressIndicator(
                        color = AgroOrange,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action button to open directly in Google Maps/Browser for voice-activated directions
            Button(
                onClick = {
                    val targetMapUrl = "https://maps.google.com/maps?vet=10CAAQoqAOahcKEwjo-uLchtSUAxUAAAAAHQAAAAAQBg..i&pvq=Cg0vZy8xMW1jcnd0MTY5Ih0KF2JhanJhbmcgYWdyaSBpbXBsZW1lbnRzEAIYAw&lqi=ChdiYWpyYW5nIGFncmkgaW1wbGVtZW50c0jJifL9rrOAgAhaJRAAEAEQAhgAGAEYAiIXYmFqcmFuZyBhZ3JpIGltcGxlbWVudHOSASNhZ3JpY3VsdHVyYWxfbWFjaGluZXJ5X21hbnVmYWN0dXJlcg&fvr=1&cs=0&um=1&ie=UTF-8&fb=1&gl=in&sa=X&ftid=0x3be0451942e76f49:0xb24d1435380927be"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(targetMapUrl)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().testTag("open_gmaps_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AgroOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Navigate Location",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open on Google Maps for GPS Directions",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ContactSection(
    context: Context,
    inquiryName: String,
    onNameChanged: (String) -> Unit,
    inquiryMobile: String,
    onMobileChanged: (String) -> Unit,
    productSelection: String,
    onProductSelectionChanged: (String) -> Unit,
    inquiryMessage: String,
    onMessageChanged: (String) -> Unit,
    isSubmitting: Boolean,
    onFormSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "GET INSTANT INQUIRY ESTIMATE",
                style = MaterialTheme.typography.labelSmall,
                color = AgroOrange,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Contact Vijay Solanki & Bhavesh Solanki",
                style = MaterialTheme.typography.titleMedium,
                color = AgroDark,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Text Inputs
            OutlinedTextField(
                value = inquiryName,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inquiry_name_input"),
                label = { Text("Your Dynamic Name") },
                leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = "User") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroGreen,
                    focusedLabelColor = AgroGreen
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inquiryMobile,
                onValueChange = onMobileChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inquiry_mobile_input"),
                label = { Text("Active Mobile Number") },
                leadingIcon = { Icon(Icons.Filled.Call, contentDescription = "Phone") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroGreen,
                    focusedLabelColor = AgroGreen
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Product selector
            var expandedDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = productSelection,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Target Machinery Implement") },
                    leadingIcon = { Icon(Icons.Filled.Build, contentDescription = "Machine") },
                    trailingIcon = {
                        IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                            Icon(
                                imageVector = if (expandedDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Dropdown Arrow"
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    ProductData.products.forEach { prod ->
                        DropdownMenuItem(
                            text = { Text(prod.name) },
                            onClick = {
                                onProductSelectionChanged(prod.name)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inquiryMessage,
                onValueChange = onMessageChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                label = { Text("Requirement Description (Optional)") },
                shape = RoundedCornerShape(14.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroGreen,
                    focusedLabelColor = AgroGreen
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Call to action
            Button(
                onClick = onFormSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_inquiry_button"),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), // High-fidelity WhatsApp green
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Submit Request",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Quote on WhatsApp 💬",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FooterSection(
    onHomeClick: () -> Unit,
    onProductsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AgroGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Build, contentDescription = "S", tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Text("JYOTI & BAJRANG AGRI IMPLEMENTS", fontWeight = FontWeight.Black, color = AgroDark, fontSize = 13.sp)
        }

        Text(
            text = "Quality. Innovation. Durability.",
            fontSize = 10.sp,
            color = AgroOrange,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick navigation
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("HOME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgroSlate, modifier = Modifier.clickable { onHomeClick() })
            Text("PRODUCTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgroSlate, modifier = Modifier.clickable { onProductsClick() })
            Text("ABOUT US", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgroSlate, modifier = Modifier.clickable { })
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = AgroSilver)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "© 2026 JYOTI & BAJRANG AGRI IMPLEMENTS.\nOwners: Vijay Solanki & Bhavesh Solanki. All rights reserved.",
            fontSize = 9.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun FloatingActionHub(
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Direct Call to Owners (Vijay Solanki & Bhavesh Solanki)
        FloatingActionButton(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:8980030865")
                }
                context.startActivity(intent)
            },
            containerColor = AgroOrange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("floating_call_button")
        ) {
            Icon(Icons.Filled.Call, contentDescription = "Call Owners")
        }

        // WhatsApp Chat
        FloatingActionButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/918980030865?text=Hello JYOTI / BAJRANG AGRI IMPLEMENTS! I would like to inquire about agricultural machinery.")
                }
                context.startActivity(intent)
            },
            containerColor = Color(0xFF22C55E),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("floating_whatsapp_button")
        ) {
            Text("💬", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// PRODUCT DETAILS DIALOG POPULAR (Holographic inspector layout)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onInquireClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onInquireClick,
                colors = ButtonDefaults.buttonColors(containerColor = AgroOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Inquire Spec Price")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = AgroSlate)
            }
        },
        title = {
            Column {
                Text(
                    text = "SPECIFICATION LAB: ${product.name.uppercase()}",
                    fontSize = 10.sp,
                    color = AgroOrange,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = product.name,
                    fontWeight = FontWeight.ExtraBold,
                    color = AgroDark,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Mini Field Video and HD Image Player inside the Dialog!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp) // Generous height for comfortable play/rendering inside details
                        .clip(RoundedCornerShape(16.dp))
                        .background(AgroLightBg)
                ) {
                    ThreeDVisualizer(product = product)
                }

                Text(
                    text = product.shortDescription,
                    fontSize = 11.5.sp,
                    color = AgroSlate,
                    lineHeight = 15.sp
                )

                HorizontalDivider(color = AgroSilver)

                if (product.id == "rotavator") {
                    Text(
                        text = "REGULAR ROTARY TILLER SERIES SPEC SHEET:",
                        color = AgroOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    // Unified scrolling state for flawless synchronized scrolling between header and body columns!
                    val tableScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AgroSilver, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AgroDark)
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MODEL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(115.dp)
                                    .padding(start = 12.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(tableScrollState),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("SRT-4", "SRT-5", "SRT-5.5", "SRT-6", "SRT-7").forEach { srtModel ->
                                    Box(
                                        modifier = Modifier
                                            .width(75.dp)
                                            .background(AgroOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = srtModel,
                                            color = AgroOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Data Specs Rows representation
                        val specRows = listOf(
                            "Overall Length" to listOf("1414 mm", "1760 mm", "1880 mm", "2026 mm", "2259 mm"),
                            "Overall Width" to listOf("959 mm", "959 mm", "959 mm", "959 mm", "959 mm"),
                            "Overall Height" to listOf("1135 mm", "1135 mm", "1135 mm", "1135 mm", "1135 mm"),
                            "Tractor Power" to listOf("40-55 HP\n(30-41 KW)", "45-60 HP\n(34-45 KW)", "50-65 HP\n(37-48 KW)", "55-70 HP\n(41-52 KW)", "65-80 HP\n(49-60 KW)"),
                            "3-Point Hitch" to listOf("Cat - II", "Cat - II", "Cat - II", "Cat - II", "Cat - II"),
                            "No. of Tines (L/C)" to listOf("30", "36", "42", "42/48", "48"),
                            "No. of Tines (Spike)" to listOf("34 & 46", "37 & 48", "52", "46 & 58", "70"),
                            "Transmission" to listOf("Gear Drive", "Gear Drive", "Gear Drive", "Gear Drive", "Gear Drive"),
                            "Max. Depth" to listOf("152 mm / 6\"", "152 mm / 6\"", "152 mm / 6\"", "152 mm / 6\"", "152 mm / 6\""),
                            "Safety Device" to listOf("Shear Bolt", "Shear Bolt", "Shear Bolt", "Shear Bolt", "Shear Bolt"),
                            "Weight" to listOf("374 Kg", "410 Kg", "436 Kg", "447 Kg", "484 Kg")
                        )

                        specRows.forEachIndexed { rowIndex, (label, values) ->
                            val bg = if (rowIndex % 2 == 0) AgroLightBg else Color.White
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = AgroSlate,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .width(115.dp)
                                        .padding(start = 12.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(tableScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    values.forEach { valStr ->
                                        Text(
                                            text = valStr,
                                            color = AgroDark,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(75.dp),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                            if (rowIndex < specRows.size - 1) {
                                HorizontalDivider(color = AgroSilver.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AgroOrangeLight, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👉", fontSize = 14.sp)
                            Text(
                                text = "Swipe the grid columns left/right to compare models SRT-4 to SRT-7 side-by-side easily!",
                                color = AgroOrange,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 13.sp
                            )
                        }
                    }
                } else if (product.id == "disc_harrow") {
                    Text(
                        text = "JYOTI ROTARY DISC HARROW SPEC SHEET:",
                        color = AgroOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    val tableScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AgroSilver, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AgroDark)
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DESCRIPTION",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(115.dp)
                                    .padding(start = 12.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(tableScrollState),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("5-DISC", "7-DISC", "9-DISC").forEach { discModel ->
                                    Box(
                                        modifier = Modifier
                                            .width(75.dp)
                                            .background(AgroOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = discModel,
                                            color = AgroOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Data Specs Rows representation
                        val specRows = listOf(
                            "Overall Length" to listOf("1700 mm", "2100 mm", "2500 mm"),
                            "Overall Width" to listOf("1900 mm", "1900 mm", "1900 mm"),
                            "Overall Height" to listOf("1300 mm", "1300 mm", "1300 mm"),
                            "Working Width" to listOf("1300 mm", "1700 mm", "2100 mm"),
                            "Working Depth" to listOf("100 to 175 mm", "100 to 175 mm", "100 to 175 mm"),
                            "No. of Disc" to listOf("5 - Disc", "7 - Disc", "9 - Disc"),
                            "Transmission" to listOf("Chain Drive", "Chain Drive", "Chain Drive"),
                            "Tractor HP" to listOf("35 to 45 HP", "45 to 60 HP", "60+ HP"),
                            "Weight Approx" to listOf("430 Kg", "480 Kg", "550 Kg"),
                            "Disc Diameter" to listOf("610 mm", "610 mm", "610 mm"),
                            "Tractor RPM" to listOf("540 RPM", "540 RPM", "540 RPM")
                        )

                        specRows.forEachIndexed { rowIndex, (label, values) ->
                            val bg = if (rowIndex % 2 == 0) AgroLightBg else Color.White
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = AgroSlate,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .width(115.dp)
                                        .padding(start = 12.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(tableScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    values.forEach { valStr ->
                                        Text(
                                            text = valStr,
                                            color = AgroDark,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(75.dp),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                            if (rowIndex < specRows.size - 1) {
                                HorizontalDivider(color = AgroSilver.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AgroOrangeLight, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👉", fontSize = 14.sp)
                            Text(
                                text = "Swipe the grid columns left/right to compare models 5-DISC to 9-DISC side-by-side easily!",
                                color = AgroOrange,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 13.sp
                            )
                        }
                    }
                } else if (product.id == "rotary_tiller") {
                    Text(
                        text = "JYOTI REVERSE FORWARD ROTARY TILLER SPEC SHEET:",
                        color = AgroOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    val tableScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AgroSilver, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AgroDark)
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHARACTERISTICS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(115.dp)
                                    .padding(start = 12.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(tableScrollState),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("RF 60", "RF 80", "RF 90", "RF 100", "RF 120").forEach { rfModel ->
                                    Box(
                                        modifier = Modifier
                                            .width(75.dp)
                                            .background(AgroOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rfModel,
                                            color = AgroOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Data Specs Rows representation
                        val specRows = listOf(
                            "Overall Length (foot)" to listOf("2.5ft", "3ft", "3.5ft", "4ft", "4.5ft"),
                            "Overall Length (mm)" to listOf("798 mm", "958 mm", "1055 mm", "1215 mm", "1415 mm"),
                            "Overall Width (mm)" to listOf("633 mm", "790 mm", "992 mm", "1048 mm", "1248 mm"),
                            "Tractor Power (HP)" to listOf("15-22 & 13-19", "18-25 & 15-21", "18-25 HP", "20-25 HP", "25-30 HP"),
                            "Three Point Hitch" to listOf("CAT-1", "CAT-1", "CAT-1", "CAT-1", "CAT-1"),
                            "No. of Blades" to listOf("18", "20", "22", "24", "30"),
                            "PTO Input Speed" to listOf("540 RPM", "540 RPM", "540 RPM", "540 RPM", "540 RPM"),
                            "Rotor Shaft RPM@540" to listOf("258 RPM", "258 RPM", "258 RPM", "258 RPM", "258 RPM"),
                            "Transmission Type" to listOf("CHAIN", "CHAIN", "CHAIN", "CHAIN", "CHAIN"),
                            "MAX Depth (mm)" to listOf("150 mm", "150 mm", "150 mm", "150 mm", "150 mm"),
                            "Rotor Tube Diam." to listOf("73 mm", "73 mm", "73 mm", "73 mm", "73 mm"),
                            "Weight Approx." to listOf("155 Kg", "165 Kg", "173 Kg", "183 Kg", "198 Kg"),
                            "Rotor Swing Diam." to listOf("484 mm", "484 mm", "484 mm", "484 mm", "484 mm")
                        )

                        specRows.forEachIndexed { rowIndex, (label, values) ->
                            val bg = if (rowIndex % 2 == 0) AgroLightBg else Color.White
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = AgroSlate,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .width(115.dp)
                                        .padding(start = 12.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(tableScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    values.forEach { valStr ->
                                        Text(
                                            text = valStr,
                                            color = AgroDark,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(75.dp),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                            if (rowIndex < specRows.size - 1) {
                                HorizontalDivider(color = AgroSilver.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AgroOrangeLight, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👉", fontSize = 14.sp)
                            Text(
                                text = "Swipe the grid columns left/right to compare models RF 60 to RF 120 side-by-side easily!",
                                color = AgroOrange,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🚨", fontSize = 14.sp)
                            Text(
                                text = "NOTE: As per your Requirements Razor Will be Attached.",
                                color = Color(0xFFB91C1C),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 13.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "ENGINEERING DESIGN PARAMS:",
                        color = AgroDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    // Key - Value Specifications List
                    product.specifications.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AgroLightBg, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
                                color = AgroSlate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = value,
                                color = AgroGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
fun InquiriesHistorySection(
    inquiries: List<Inquiry>,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = "Quotes History",
                    tint = AgroGreen,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "My Saved Quote Requests",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgroDark
                    )
                    Text(
                        text = "Offline local app storage database",
                        fontSize = 11.sp,
                        color = AgroSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (inquiries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgroLightBg, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MailOutline,
                            contentDescription = "No past quotes",
                            tint = AgroSlate.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No In-App Quote Requests Filed Yet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroSlate,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Fill in the contact form above to request an instant quote directly via this app!",
                            fontSize = 10.sp,
                            color = AgroSlate,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    inquiries.forEach { inquiry ->
                        val formattedDate = try {
                            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(inquiry.timestamp))
                        } catch (e: Exception) {
                            "Just now"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AgroLightBg, RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = AgroLightBg.copy(alpha = 0.5f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Build,
                                            contentDescription = "Product",
                                            tint = AgroOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = inquiry.productSelected,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AgroDark
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { onDelete(inquiry.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete from local db",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Client",
                                        tint = AgroSlate,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${inquiry.customerName} (${inquiry.customerPhone})",
                                        fontSize = 11.sp,
                                        color = AgroDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (inquiry.message.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = inquiry.message,
                                        fontSize = 11.sp,
                                        color = AgroSlate,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = AgroSlate.copy(alpha = 0.1f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saved: $formattedDate",
                                        fontSize = 9.sp,
                                        color = AgroSlate
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AgroGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SECURELY SENT VIA APP",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AgroGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InquirySuccessDialog(
    inquiry: Inquiry?,
    onDismiss: () -> Unit
) {
    if (inquiry == null) return

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Excellent", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val formattedMsg = """
                        *FOLLOW UP: REQUEST FOR QUOTE*
                        👤 Client Name: ${inquiry.customerName}
                        📱 Contact: ${inquiry.customerPhone}
                        ⚙️ Product: ${inquiry.productSelected}
                        ✉️ Note: ${inquiry.message}
                    """.trimIndent()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/918980030865?text=${Uri.encode(formattedMsg)}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp is not available on this device.", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF22C55E))
            ) {
                Text("Manually Share on WA 💬", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = AgroGreen,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Quote Request Recorded!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = AgroDark
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Thank you! Your quote request has been securely processed and stored inside the Bajrang Agri Implements local app database instead of forcing a WhatsApp redirect.",
                    fontSize = 11.5.sp,
                    color = AgroDark,
                    lineHeight = 16.sp
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AgroLightBg, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = AgroLightBg.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "MACHINE REQUESTED:",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroOrange
                        )
                        Text(
                            text = inquiry.productSelected,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CONTACT DETAILS:",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroSlate
                        )
                        Text(
                            text = "${inquiry.customerName} (${inquiry.customerPhone})",
                            fontSize = 11.sp,
                            color = AgroDark
                        )
                    }
                }

                Text(
                    text = "Vijay Solanki & Bhavesh Solanki will analyze your request and reach out directly to your registered number.",
                    fontSize = 10.5.sp,
                    color = AgroSlate,
                    lineHeight = 14.sp
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
