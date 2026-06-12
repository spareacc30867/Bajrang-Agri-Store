package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class Vertex3D(val x: Float, val y: Float, val z: Float) {
    fun rotateX(angleRad: Float): Vertex3D {
        val c = cos(angleRad).toFloat()
        val s = sin(angleRad).toFloat()
        return Vertex3D(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(angleRad: Float): Vertex3D {
        val c = cos(angleRad).toFloat()
        val s = sin(angleRad).toFloat()
        return Vertex3D(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(angleRad: Float): Vertex3D {
        val c = cos(angleRad).toFloat()
        val s = sin(angleRad).toFloat()
        return Vertex3D(x * c - y * s, x * s + y * c, z)
    }
}

data class Face3D(
    val vertexIndices: List<Int>,
    val color: Color,
    val isOutlineOnly: Boolean = false,
    val label: String? = null
) {
    fun getAverageZ(projectedVertices: List<Vertex3D>): Float {
        if (vertexIndices.isEmpty()) return 0f
        var sum = 0f
        for (idx in vertexIndices) {
            if (idx in projectedVertices.indices) {
                sum += projectedVertices[idx].z
            }
        }
        return sum / vertexIndices.size
    }
}

data class Mesh3D(
    val name: String,
    val vertices: List<Vertex3D>,
    val faces: List<Face3D>
)

object MeshGenerator {

    // Helper to generate a 3D Box (cuboid)
    private fun createBox(
        cx: Float, cy: Float, cz: Float,
        dx: Float, dy: Float, dz: Float,
        color: Color,
        startIdx: Int,
        label: String? = null
    ): Pair<List<Vertex3D>, List<Face3D>> {
        val halfX = dx / 2
        val halfY = dy / 2
        val halfZ = dz / 2

        val localVertices = listOf(
            Vertex3D(cx - halfX, cy - halfY, cz - halfZ), // 0
            Vertex3D(cx + halfX, cy - halfY, cz - halfZ), // 1
            Vertex3D(cx + halfX, cy + halfY, cz - halfZ), // 2
            Vertex3D(cx - halfX, cy + halfY, cz - halfZ), // 3
            Vertex3D(cx - halfX, cy - halfY, cz + halfZ), // 4
            Vertex3D(cx + halfX, cy - halfY, cz + halfZ), // 5
            Vertex3D(cx + halfX, cy + halfY, cz + halfZ), // 6
            Vertex3D(cx - halfX, cy + halfY, cz + halfZ)  // 7
        )

        // 6 faces
        val localFaces = listOf(
            Face3D(listOf(0, 1, 2, 3).map { it + startIdx }, color, label = label), // Front
            Face3D(listOf(1, 5, 6, 2).map { it + startIdx }, darkenColor(color, 0.85f), label = label), // Right
            Face3D(listOf(5, 4, 7, 6).map { it + startIdx }, darkenColor(color, 0.7f), label = label), // Back
            Face3D(listOf(4, 0, 3, 7).map { it + startIdx }, darkenColor(color, 0.85f), label = label), // Left
            Face3D(listOf(3, 2, 6, 7).map { it + startIdx }, darkenColor(color, 0.95f), label = label), // Top
            Face3D(listOf(4, 5, 1, 0).map { it + startIdx }, darkenColor(color, 0.6f), label = label)  // Bottom
        )

        return Pair(localVertices, localFaces)
    }

    private fun darkenColor(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    // Generator 1: Disc Harrow
    // Includes a thick chassis frame, heavy link coupling, and several curved metal cutting discs
    fun generateDiscHarrow(): Mesh3D {
        val vertices = mutableListOf<Vertex3D>()
        val faces = mutableListOf<Face3D>()

        val steelColor = Color(0xFF64748B) // Sleek Metallic Slate
        val orangeChassis = Color(0xFFF97316) // Brand orange frame
        val discColor = Color(0xFF94A3B8) // Shiny iron disc gray

        // -- 1. Chassis Main Beam (Central Box) --
        val (bVerts, bFaces) = createBox(0f, -20f, 0f, 160f, 10f, 12f, orangeChassis, vertices.size, "Orange Reinforced Steel Frame")
        vertices.addAll(bVerts)
        faces.addAll(bFaces)

        // -- 2. Drawbar / Towing Hitch (Pointing Forward) --
        // Triangle shape pointing forward
        val hitchStartIdx = vertices.size
        vertices.add(Vertex3D(-20f, -15f, -10f)) // Left back hitch joint
        vertices.add(Vertex3D(20f, -15f, -10f))  // Right back hitch joint
        vertices.add(Vertex3D(0f, -15f, -60f))   // Hitch coupling point

        faces.add(Face3D(listOf(hitchStartIdx, hitchStartIdx + 1, hitchStartIdx + 2), Color(0xFF1E293B), label = "3-Point Towing Hitch Link"))

        // Small yellow coupling cylinder at the tip
        val couplingStartIdx = vertices.size
        val (cVerts, cFaces) = createBox(0f, -15f, -62f, 8f, 6f, 8f, Color(0xFFEAB308), couplingStartIdx, "Towing Eye Adapter")
        vertices.addAll(cVerts)
        faces.addAll(cFaces)

        // -- 3. Four Gang Shafts & Discs (Two front, Two rear) --
        // We render axles and circles representing the sharp spherical disc blades
        // Front gang left: slanted axle at Y=-10, Z=-15
        generateGang(
            cx = -40f, cz = -15f,
            length = 70f,
            yawAngle = 0.25f, // Slanted forward-left
            discColor = discColor,
            steelColor = steelColor,
            vertices = vertices,
            faces = faces,
            gangLabelText = "Front-Left Gang Disc Assembly"
        )

        // Front gang right: slanted axle
        generateGang(
            cx = 40f, cz = -15f,
            length = 70f,
            yawAngle = -0.25f, // Slanted forward-right
            discColor = discColor,
            steelColor = steelColor,
            vertices = vertices,
            faces = faces,
            gangLabelText = "Front-Right Gang Disc Assembly"
        )

        // Rear gang left: slanted backward
        generateGang(
            cx = -40f, cz = 15f,
            length = 70f,
            yawAngle = -0.15f,
            discColor = discColor,
            steelColor = steelColor,
            vertices = vertices,
            faces = faces,
            gangLabelText = "Rear-Left Gang Disc Assembly"
        )

        // Rear gang right: slanted backward
        generateGang(
            cx = 40f, cz = 15f,
            length = 70f,
            yawAngle = 0.15f,
            discColor = discColor,
            steelColor = steelColor,
            vertices = vertices,
            faces = faces,
            gangLabelText = "Rear-Right Gang Disc Assembly"
        )

        return Mesh3D("Disc Harrow", vertices, faces)
    }

    private fun generateGang(
        cx: Float, cz: Float,
        length: Float,
        yawAngle: Float,
        discColor: Color,
        steelColor: Color,
        vertices: MutableList<Vertex3D>,
        faces: MutableList<Face3D>,
        gangLabelText: String
    ) {
        // An axle bar containing 4 circular discs
        val axleSteps = 4
        val stepSize = length / (axleSteps - 1)

        for (i in 0 until axleSteps) {
            val offsetMultiplier = i - (axleSteps - 1) / 2.0f
            val localX = cx + offsetMultiplier * stepSize * cos(yawAngle)
            val localZ = cz + offsetMultiplier * stepSize * sin(yawAngle)
            // Draw a disc centered at (localX, -8, localZ)
            val discCenter = Vertex3D(localX, -8f, localZ)

            // Let's draw a disc as a 3D dome (a circular base with a popped-out center cone)
            val baseIdx = vertices.size
            vertices.add(discCenter) // Center point (apex of disc concavity)

            val radius = 18f
            val numSegments = 10
            for (s in 0 until numSegments) {
                val circleAngle = (2 * PI * s / numSegments).toFloat()
                // Rotate the circle according to the gang yaw angle so it aligns perpendicularly with the axle
                val circleX = radius * cos(circleAngle)
                val circleY = radius * sin(circleAngle)

                // Project to rotate around Y axis by yawAngle:
                val rotatedX = circleX * cos(yawAngle)
                val rotatedY = circleY
                val rotatedZ = -circleX * sin(yawAngle)

                // Add vertices:
                // Slightly popped forward in the axle's direction to make it look concave
                val popX = 4f * sin(yawAngle)
                val popZ = 4f * cos(yawAngle)

                vertices.add(
                    Vertex3D(
                        discCenter.x + rotatedX + popX,
                        discCenter.y + rotatedY,
                        discCenter.z + rotatedZ + popZ
                    )
                )
            }

            // Connect with faces:
            for (s in 0 until numSegments) {
                val p1 = baseIdx + 1 + s
                val p2 = baseIdx + 1 + ((s + 1) % numSegments)
                val cIndex = baseIdx // Apex

                // Alternating shade of shiny carbon steel disk
                val sColor = if (s % 2 == 0) discColor else darkenColor(discColor, 0.9f)
                faces.add(Face3D(listOf(cIndex, p1, p2), sColor, label = "$gangLabelText : Sharp Concave Disc $i"))
            }

            // Draw a small center chrome axle connector cube
            val connIdx = vertices.size
            val (connVerts, connFaces) = createBox(
                discCenter.x, discCenter.y, discCenter.z,
                6f, 6f, 6f, Color(0xFF334155),
                connIdx,
                "$gangLabelText : Axle Spool Hub"
            )
            vertices.addAll(connVerts)
            faces.addAll(connFaces)
        }
    }


    // Generator 2: Reverse Forward Rotary Tiller
    // Features dual gearboxes, chain cover, adjustable frame, and multiple spiral soil cutting tines/blades
    fun generateReverseForwardRotary(): Mesh3D {
        val vertices = mutableListOf<Vertex3D>()
        val faces = mutableListOf<Face3D>()

        val brandGreen = Color(0xFF166534) // High class green frame
        val steelBlade = Color(0xFF475569) // Hardened steel blades
        val goldAccent = Color(0xFFF59E0B) // Gearbox/link color

        // -- 1. Main Top Frame Beam --
        val (fVerts, fFaces) = createBox(0f, -25f, 0f, 150f, 12f, 14f, brandGreen, vertices.size, "Primary Modular Box Beam")
        vertices.addAll(fVerts)
        faces.addAll(fFaces)

        // -- 2. Large Central Multi-Speed Gearbox --
        val (gbVerts, gbFaces) = createBox(0f, -24f, -12f, 24f, 32f, 20f, goldAccent, vertices.size, "Heavy-Duty Dual Direction Gearbox")
        vertices.addAll(gbVerts)
        faces.addAll(gbFaces)

        // Central PTO shaft input snout
        val (ptoVerts, ptoFaces) = createBox(0f, -24f, -26f, 10f, 10f, 14f, Color(0xFF1E293B), vertices.size, "PTO Spline Input Protector")
        vertices.addAll(ptoVerts)
        faces.addAll(ptoFaces)

        // -- 3. Side Drive Chain Cover (Shielding polygon on Left Side) --
        val sideChainIdx = vertices.size
        val (scVerts, scFaces) = createBox(-73f, -12f, 0f, 10f, 40f, 18f, brandGreen, sideChainIdx, "Oil-Bath Side Gear Drive Cover")
        vertices.addAll(scVerts)
        faces.addAll(scFaces)

        // Simple shield on Right Side
        val sidePlateIdx = vertices.size
        val (spVerts, spFaces) = createBox(73f, -12f, 0f, 6f, 40f, 18f, brandGreen, sidePlateIdx, "Heavy-Duty Side Bearing support plate")
        vertices.addAll(spVerts)
        faces.addAll(spFaces)

        // -- 4. Blade Rotor & Helical Curved Blades --
        // A rotor cylinder ranging from x = -70 to 70
        val numClanges = 6
        val flangeSpacing = 135f / (numClanges - 1)

        for (fl in 0 until numClanges) {
            val flangeX = -67.5f + fl * flangeSpacing

            // On each flange, let's place 4 tines sticking out perpendicular, rotated by (fl * 45 degrees) for helical spiral
            val baseAngleOffset = (fl * PI / 4).toFloat()

            for (t in 0 until 4) {
                val bladeAngle = baseAngleOffset + (t * PI / 2).toFloat()
                val bladeLength = 22f

                // Direct vector radiating out from the rotor center
                val cosA = cos(bladeAngle).toFloat()
                val sinA = sin(bladeAngle).toFloat()

                val baseIdx = vertices.size
                // Inner mounting hub point
                vertices.add(Vertex3D(flangeX, 6f, 0f))
                // Middle curve joint
                vertices.add(Vertex3D(flangeX, 6f + (bladeLength - 6) * sinA, (bladeLength - 6) * cosA))
                // Curved sharp tip: pointing sideways in L-shape along X
                val shapeDirectionX = if (t % 2 == 0) -5f else 5f
                vertices.add(Vertex3D(flangeX + shapeDirectionX, 6f + bladeLength * sinA, bladeLength * cosA))

                // Connect these 3 vertex lines as a thick heavy blade tilling shape
                faces.add(
                    Face3D(
                        listOf(baseIdx, baseIdx + 1, baseIdx + 2),
                        if (t % 2 == 0) steelBlade else darkenColor(steelBlade, 0.8f),
                        label = "L-Type Soil Tiller Blade"
                    )
                )
            }
        }

        // -- 5. Rear Trailing Sheet (Cover Board, slanted down) --
        // Represented as a flat slanted polygon across the back
        val bBoardStartIdx = vertices.size
        vertices.add(Vertex3D(-70f, -18f, 15f)) // Left-top link
        vertices.add(Vertex3D(70f, -18f, 15f))  // Right-top link
        vertices.add(Vertex3D(68f, 12f, 32f))   // Right-bottom sheet
        vertices.add(Vertex3D(-68f, 12f, 32f))  // Left-bottom sheet

        faces.add(Face3D(listOf(bBoardStartIdx, bBoardStartIdx + 1, bBoardStartIdx + 2, bBoardStartIdx + 3), brandGreen, label = "Adjustable Rear Trailing Sheet Board"))

        return Mesh3D("Reverse Forward Rotary", vertices, faces)
    }


    // Generator 3: Rotavator
    // Features robust red/orange main shield structure, high-end side box drive, heavy center PTO mount, and spiral curved blades
    fun generateRotavator(): Mesh3D {
        val vertices = mutableListOf<Vertex3D>()
        val faces = mutableListOf<Face3D>()

        val deepOrange = Color(0xFFEA580C) // Glowing Reddish Orange for Rotavator Body
        val steelBlade = Color(0xFF334155) // Slate metal
        val supportGrey = Color(0xFF94A3B8) // Bright iron brackets

        // -- 1. Curved Outer Shield/Hood --
        // We draw 4 horizontal segments of a curved plate to simulate a true curved hood
        val numXSegments = 8
        val numYSegments = 4
        val lengthX = 145f
        val startX = -lengthX / 2

        // Hood curvature model: as we go back in Z, Y bends down
        val zPoints = listOf(-16f, -4f, 8f, 20f)
        val yPoints = listOf(-26f, -28f, -22f, -10f)

        val hoodBaseIdx = vertices.size

        // Add all shield grid coordinates:
        for (yi in 0 until numYSegments) {
            val currentY = yPoints[yi]
            val currentZ = zPoints[yi]
            for (xi in 0 until numXSegments) {
                val currentX = startX + xi * (lengthX / (numXSegments - 1))
                vertices.add(Vertex3D(currentX, currentY, currentZ))
            }
        }

        // Connect shield grids as rectangles:
        for (yi in 0 until numYSegments - 1) {
            for (xi in 0 until numXSegments - 1) {
                val idx0 = hoodBaseIdx + yi * numXSegments + xi
                val idx1 = idx0 + 1
                val idx2 = idx0 + numXSegments + 1
                val idx3 = idx0 + numXSegments

                // Alternate color shading for realistic surface texture shadows
                val hColor = if (xi % 2 == 0) deepOrange else darkenColor(deepOrange, 0.9f)
                faces.add(
                    Face3D(
                        listOf(idx0, idx1, idx2, idx3),
                        hColor,
                        label = "Curved Metal Protective Shield Hood"
                    )
                )
            }
        }

        // -- 2. Rigid Top Canopy Mast --
        // Triangle shape above center for links
        val canopyStartIdx = vertices.size
        vertices.add(Vertex3D(-12f, -28f, -5f)) // Left frame mount
        vertices.add(Vertex3D(12f, -28f, -5f))  // Right frame mount
        vertices.add(Vertex3D(0f, -56f, -3f))   // Top link connection pin

        faces.add(Face3D(listOf(canopyStartIdx, canopyStartIdx + 1, canopyStartIdx + 2), supportGrey, label = "Cast Iron 3-Point Linkage Mast"))

        // -- 3. Dynamic Side Gearbox Plates (Plates closing off the left and right ends) --
        val (lPlateVerts, lPlateFaces) = createBox(-73f, -16f, 2f, 6f, 32f, 30f, deepOrange, vertices.size, "Reinforced Left Side Hub Assembly")
        vertices.addAll(lPlateVerts)
        faces.addAll(lPlateFaces)

        val (rPlateVerts, rPlateFaces) = createBox(73f, -16f, 2f, 6f, 32f, 30f, deepOrange, vertices.size, "Reinforced Right Side Bearing Flange")
        vertices.addAll(rPlateVerts)
        faces.addAll(rPlateFaces)

        // -- 4. Spinning Blades --
        // Helical curve blades (Carbon Steel J-Blades)
        val numBladeFlanges = 7
        val bladeSpacing = 140f / (numBladeFlanges - 1)

        for (bf in 0 until numBladeFlanges) {
            val fX = -70f + bf * bladeSpacing

            // Spiral index offset: rotates each set by 60 degs
            val phaseOffset = (bf * PI / 3).toFloat()

            // 4 blades per flange
            for (b in 0 until 4) {
                val bAngle = phaseOffset + (b * PI / 2).toFloat()
                val radius = 24f

                val bIdx = vertices.size
                // Inner mount
                vertices.add(Vertex3D(fX, -4f, 0f))
                // Curve hinge
                vertices.add(Vertex3D(fX, -4f + (radius - 8f) * sin(bAngle).toFloat(), (radius - 8f) * cos(bAngle).toFloat()))
                // Standard J-Blade outward tip
                val tOffset = if (b % 2 == 0) -7f else 7f
                vertices.add(Vertex3D(fX + tOffset, -4f + radius * sin(bAngle).toFloat(), radius * cos(bAngle).toFloat()))

                faces.add(
                    Face3D(
                        listOf(bIdx, bIdx + 1, bIdx + 2),
                        if (b % 2 == 0) steelBlade else darkenColor(steelBlade, 0.85f),
                        label = "J-Type Boron Steel Soil Blade"
                    )
                )
            }
        }

        // Central Gearbox block directly in the center
        val centerGbIdx = vertices.size
        val (cGbVerts, cGbFaces) = createBox(0f, -32f, -10f, 18f, 22f, 18f, Color(0xFF1E293B), centerGbIdx, "Heavy-Duty Multi-Speed Drive Transmission")
        vertices.addAll(cGbVerts)
        faces.addAll(cGbFaces)

        return Mesh3D("Rotavator Tiller", vertices, faces)
    }
}
