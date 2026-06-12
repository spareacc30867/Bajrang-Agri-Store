package com.example.model

data class Product(
    val id: String,
    val name: String,
    val tagline: String,
    val shortDescription: String,
    val imageUrl: String,
    val defaultVideoUrl: String,
    val specifications: Map<String, String>,
    val mesh: Mesh3D
)

object ProductData {
    val products = listOf(
        Product(
            id = "disc_harrow",
            name = "Disc Harrow",
            tagline = "Engineering Masterpiece for Deep Tillage",
            shortDescription = "Premium heavy-duty double action disc harrow with self-sharpening boron steel disks. Perfect for severe soil cutting, breaking stubborn clods, and field clearing operations in all soil profiles.",
            // Converting Dropbox preview URL to direct download URL (dl=1) to ensure Coil downloads the raw image smoothly!
            imageUrl = "https://www.dropbox.com/scl/fi/03yr5yof3xtsfxar6crop/IMG-20250910-WA0004.jpg?rlkey=y1zzpuwy6y98tfyqqyf9iyk2x&st=qticljft&dl=1",
            defaultVideoUrl = "https://youtu.be/fumvOV8NZtU?si=qfq7ZUrgY4gzvVbk",
            specifications = mapOf(
                "Disk Category" to "Boron Carbon Steel - Concave",
                "Working Width" to "1400 - 2400 mm",
                "Weight Range" to "450 - 780 Kg",
                "Number of Disks" to "14 to 22 Sharp Blades",
                "Compatible HP" to "40 - 85 Horse Power",
                "Scraper System" to "Adjustable Rigid Steel Scrapers",
                "Coating Protection" to "Double Powder Electrostatic Shield"
            ),
            mesh = MeshGenerator.generateDiscHarrow()
        ),
        Product(
            id = "rotary_tiller",
            name = "Reverse Forward Rotary",
            tagline = "Dual Multi-Speed Multi-Direction Tiller",
            shortDescription = "High-end specialized rotary tiller equipped with reverse/forward gears. Multiplies tractor efficiency by enabling dual-direction tilling vectors, perfect for multi-crop farms.",
            imageUrl = "https://www.dropbox.com/scl/fi/3u8ujv2mv442ayh0diy6s/compressed_resized_image.jpg?rlkey=lgs1luvjshfog8r1xdoa4c326&st=xgfhomu7&dl=1",
            defaultVideoUrl = "https://youtube.com/shorts/PYQGsleRJgo?si=XHQxG4nbC-iVCZCT",
            specifications = mapOf(
                "Transmission Type" to "Oil-Bath Gear + Dual Chain Drive",
                "Operating Vector" to "Bi-Directional Forward & Reverse",
                "Blade Spec" to "Special Forged L-Type High Tensile",
                "Working Width" to "1500 - 2100 mm",
                "Blade Count" to "36 to 48 Heavy Blades",
                "Compatible HP" to "45 - 80 Horse Power",
                "Total Gear Speeds" to "4 Speed Variations"
            ),
            mesh = MeshGenerator.generateReverseForwardRotary()
        ),
        Product(
            id = "rotavator",
            name = "Rotavator Tiller",
            tagline = "The Ultimate Gold Standard in Pulverization",
            shortDescription = "Shaktiman-quality inspired premium rotavator. Delivers fine, professional seedbed preparation in single run. Includes extra-reinforced vibration dampening chassis frames.",
            imageUrl = "https://www.dropbox.com/scl/fi/zra4fadqz5nw2cvehdbgo/WhatsApp_Image_2025-12-02_at_3.08.44_PM-removebg-preview-1.png?rlkey=dxgt9k7851pu6we8juwpgv8p3&st=rcwwmf3k&dl=1",
            defaultVideoUrl = "https://youtu.be/098uz5lf8Ac?si=iZUisnB3Q7MIOltj",
            specifications = mapOf(
                "Chassis Assembly" to "Heavy-Duty Reinforced Box Structure",
                "Gear Transmission" to "Hardened Bevel PIN-Gear System",
                "Blade Profile" to "J-Type Boron Coated Blades",
                "Working Depth" to "Adjustable up to 220 mm",
                "Working Width" to "1200 - 2000 mm",
                "Compatible HP" to "35 - 75 Horse Power",
                "Trailing Board" to "Double-Spring Shock Absorber Panel"
            ),
            mesh = MeshGenerator.generateRotavator()
        )
    )
}
