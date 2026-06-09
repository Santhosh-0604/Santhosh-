package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ClosetViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    viewModel: ClosetViewModel,
    onNavigateToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stylePersonality by viewModel.stylePersonality.collectAsState()
    val currentQuestionIndex by viewModel.quizCurrentQuestionIndex.collectAsState()
    val scores by viewModel.quizScores.collectAsState()

    // 5 custom questions targeting key style preferences:
    // Color Palette, Silhouette/Fit, Go-to weekend outfit (Occasion), Inspiration (Style Icons), Fabric preferences
    val questions = remember {
        listOf(
            QuizQuestion(
                questionText = "Which color palette dominates your dream wardrobe?",
                options = listOf(
                    QuizOption("Monochromes, clean black, white, and subtle greys", "Minimalist", "⚫"),
                    QuizOption("Earthy warm tones like terracotta, sage green, and rust", "Bohemian", "🪵"),
                    QuizOption("Tailored neutrals, camel beige, navy blue, and rich cream", "Classic", "🤎"),
                    QuizOption("Vibrant statement colors, neon pops, and bold patterns", "Trendy", "⚡")
                )
            ),
            QuizQuestion(
                questionText = "How do you prefer your standard clothing silhouettes to fit?",
                options = listOf(
                    QuizOption("Streamlined, simple lines with crisp geometric shapes", "Minimalist", "📐"),
                    QuizOption("Loose, relaxed, flowy, and layered organically", "Bohemian", "🍃"),
                    QuizOption("Structured, traditional, and perfectly tailored", "Classic", "📏"),
                    QuizOption("Oversized statements, asymmetrical cuts, and bold dimensions", "Trendy", "🧩")
                )
            ),
            QuizQuestion(
                questionText = "What is your absolute go-to weekend outfit for social gatherings?",
                options = listOf(
                    QuizOption("A pristine plain t-shirt and straight-leg denim", "Minimalist", "👕"),
                    QuizOption("A flowy textured linen outfit or soft relaxed patterns", "Bohemian", "🌾"),
                    QuizOption("A customized blazer, smart trousers, and leather loafers", "Classic", "💼"),
                    QuizOption("An artistic graphic tee with high-top sneakers and utility bomber", "Trendy", "🕶️")
                )
            ),
            QuizQuestion(
                questionText = "Which fashion style figures or icons prompt your style goals the most?",
                options = listOf(
                    QuizOption("Steve Jobs & Phoebe Philo — pure functional utility", "Minimalist", "💻"),
                    QuizOption("Jane Birkin & Florence Welch — relaxed free spirits", "Bohemian", "🎻"),
                    QuizOption("Audrey Hepburn & Cary Grant — structured poise", "Classic", "🎬"),
                    QuizOption("Zendaya & Harry Styles — forward-thinking borderless statements", "Trendy", "🔮")
                )
            ),
            QuizQuestion(
                questionText = "What material, texture, or finish details draw you in first?",
                options = listOf(
                    QuizOption("Matte surfaces, clean hidden seams, and pure cotton", "Minimalist", "🧵"),
                    QuizOption("Natural textures like fleece wool, raw linen, and soft suede", "Bohemian", "🐑"),
                    QuizOption("Premium woven wool, fine cashmere, or structured gabardine", "Classic", "👔"),
                    QuizOption("Glossy finishes, futuristic mesh-knit, or bold nylon blends", "Trendy", "💿")
                )
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Intro Meta
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "AURA ALIGNMENT",
                color = BrandRose,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Style Archetype Diagnostic",
                color = BrandDarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedContent(
            targetState = currentQuestionIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() with
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "quiz_transition"
        ) { index ->
            when (index) {
                -1 -> QuizIntroView(
                    stylePersonality = stylePersonality,
                    onStartClick = { viewModel.startQuiz() },
                    onResetAesthetic = { viewModel.updateStylePersonality(null) }
                )
                in 0..4 -> {
                    val question = questions[index]
                    QuizQuestionView(
                        questionIndex = index,
                        question = question,
                        onOptionSelected = { type -> viewModel.answerQuizQuestion(type) },
                        onBackClick = { viewModel.previousQuizQuestion() }
                    )
                }
                else -> QuizResultView(
                    stylePersonality = stylePersonality ?: "Minimalist",
                    scores = scores,
                    onRetakeClick = { viewModel.startQuiz() },
                    onGoToToday = onNavigateToToday
                )
            }
        }
    }
}

@Composable
fun QuizIntroView(
    stylePersonality: String?,
    onStartClick: () -> Unit,
    onResetAesthetic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BrandRoseMutedLight),
                contentAlignment = Alignment.Center
            ) {
                Text("🎯", fontSize = 28.sp)
            }

            Text(
                text = "Discover Your Style Personality",
                color = BrandDarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Take our bespoke 5-step style diagnostic. Uncover your dominant style archetype (Minimalist, Bohemian, Classic, or Trendy) to help Aura AI tailor your daily outfit selections to look elegant, balanced, and perfectly coordinated.",
                color = BrandMutedText,
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 18.sp)
            )

            if (stylePersonality != null) {
                Divider(color = BrandBorder.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandRoseLight, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT STYLE ARCHETYPE",
                            color = BrandRose,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "✨ ${stylePersonality.uppercase()}",
                            color = BrandDarkText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onResetAesthetic,
                        colors = ButtonDefaults.textButtonColors(contentColor = BrandRose)
                    ) {
                        Text("Reset Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_quiz_button")
            ) {
                Text(
                    text = if (stylePersonality == null) "Begin Style Diagnostic" else "Retake Diagnostic Quiz",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun QuizQuestionView(
    questionIndex: Int,
    question: QuizQuestion,
    onOptionSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back and Progress text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(BrandRoseMutedLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Question",
                        tint = BrandRose,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "Question ${questionIndex + 1} of 5",
                    color = BrandRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Simple Linear Progress Bar
            LinearProgressIndicator(
                progress = (questionIndex + 1) / 5f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = BrandRose,
                trackColor = BrandBorder
            )

            Text(
                text = question.questionText,
                color = BrandDarkText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Selectable choices
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                question.options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandRoseMutedLight.copy(alpha = 0.5f))
                            .border(1.dp, BrandBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onOptionSelected(option.personalityType) }
                            .padding(14.dp)
                            .testTag("quiz_option_${option.personalityType.lowercase()}")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(option.emoji, fontSize = 18.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.text,
                                    color = BrandDarkText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultView(
    stylePersonality: String,
    scores: Map<String, Int>,
    onGoToToday: () -> Unit,
    onRetakeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val desc = when (stylePersonality) {
        "Minimalist" -> "You favor clean geometric outlines, monochromatic styling (black, off-whites, greys), and uncompromised utilitarian design. To your eye, negative space is a canvas, and clutter is noise. Aura AI will recommend pure, simple, highly uniform silhouettes for your agenda."
        "Bohemian" -> "Your aesthetic stems from natural relaxation: loose breathable weaves (linen, wools, cottons), warm layered earth colors, and rich textures. You favor artistic, organic, free-flowing drapes. Aura AI will look to curate coordinating warm-toned layered assemblies."
        "Classic" -> "True timelessness underpins your clothing selections. You seek structure, traditional tailored symmetry, premium material craftsmanship, and heritage neutrals (navy, beige, crisp corporate blues). Aura AI will recommend refined blazer alignments and poise-centric coordinates."
        else -> "You are a visual trailblazer, drawn to oversized structural statement silhouettes, bold experimental color contrasts, and technical avant-garde fabrics. Clothes for you represent self-expression and performance. Aura AI will highlight your heavy-weight outerwear and bold accents."
    }

    val archetypeEmoji = when (stylePersonality) {
        "Minimalist" -> "📐"
        "Bohemian" -> "🍃"
        "Classic" -> "💼"
        else -> "⚡"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(BrandRoseLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(archetypeEmoji, fontSize = 34.sp)
                }

                Text(
                    text = "AURA ALIGNED!",
                    color = BrandRose,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = stylePersonality,
                    color = BrandDarkText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .background(BrandRoseMutedLight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Your Wardrobe Personality Profile",
                        color = BrandRose,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = BrandBorder.copy(alpha = 0.5f))

            Text(
                text = desc,
                color = BrandMutedText,
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 19.sp)
            )

            // Let's draw some cute small progress lines representing the user's scored breakdown!
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandRoseMutedLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Archetype Score Affinity",
                    color = BrandDarkText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                scores.forEach { (type, count) ->
                    val progressFloat = count / 5f
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type, color = BrandMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${(progressFloat * 100).toInt()}% Affinity", color = BrandRose, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = progressFloat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = BrandRose,
                            trackColor = BrandBorder.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Bottom Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGoToToday,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_aesthetic_recs_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = Color.White)
                        Text("Apply & Refresh AI Suggestions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                TextButton(
                    onClick = onRetakeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Re-diagnose (Retake Quiz)", color = BrandMutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Data structures
data class QuizQuestion(
    val questionText: String,
    val options: List<QuizOption>
)

data class QuizOption(
    val text: String,
    val personalityType: String, // Minimalist, Bohemian, Classic, Trendy
    val emoji: String
)
