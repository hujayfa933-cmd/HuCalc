package com.example.calculator.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.*

/**
 * High-precision on-device mathematical reasoning and step-by-step problem solver.
 * Operates 100% offline with zero network connectivity or API key requirements.
 */
object OfflineMathEngine {

    private val calcEngine = CalculationEngine()

    /**
     * Solves a user math prompt completely offline, returning a pair of:
     * - Formatted Markdown explanation with step-by-step deduction
     * - Optional extracted answer for direct 1-tap insertion into the calculator display
     */
    fun solve(
        prompt: String,
        calculatorContext: String? = null,
        angleMode: AngleMode = AngleMode.DEG
    ): Pair<String, String?> {
        val trimmed = prompt.trim()

        // 1. Context Explanation Request ("explain this", "explain", "how was this calculated")
        if (isExplanationRequest(trimmed) && !calculatorContext.isNullOrBlank()) {
            return explainCalculatorContext(calculatorContext, angleMode)
        }

        // 2. Quadratic Equation: ax² + bx + c = 0
        val quadraticResult = trySolveQuadratic(trimmed)
        if (quadraticResult != null) {
            return quadraticResult
        }

        // 3. Linear Equation: ax + b = c
        val linearResult = trySolveLinear(trimmed)
        if (linearResult != null) {
            return linearResult
        }

        // 4. Prime Factorization ("factors of 84", "prime factors of 360", "factor 120")
        val primeResult = tryPrimeFactors(trimmed)
        if (primeResult != null) {
            return primeResult
        }

        // 5. GCD / LCM ("gcd 24 36", "lcm 12 18", "gcd(48, 180)")
        val gcdLcmResult = tryGcdLcm(trimmed)
        if (gcdLcmResult != null) {
            return gcdLcmResult
        }

        // 6. Percentage ("25% of 80", "what is 15% of 200", "percent 40 200")
        val percentResult = tryPercentage(trimmed)
        if (percentResult != null) {
            return percentResult
        }

        // 7. Calculus derivatives / integrals inquiry ("derivative of sin(x)", "d/dx x^3", "integral of x^2")
        val calculusResult = tryCalculus(trimmed)
        if (calculusResult != null) {
            return calculusResult
        }

        // 8. Trigonometric values inquiry ("sin 45", "cos 60", "tan 30")
        val trigResult = tryTrigExact(trimmed, angleMode)
        if (trigResult != null) {
            return trigResult
        }

        // 9. Scientific Constants ("speed of light", "planck constant", "gravitational constant")
        val constResult = tryScientificConstant(trimmed)
        if (constResult != null) {
            return constResult
        }

        // 10. General Math Expression Step-by-Step Breakdown
        val expressionResult = tryStepByStepEvaluation(trimmed, calculatorContext, angleMode)
        if (expressionResult != null) {
            return expressionResult
        }

        // Fallback: Helpful offline assistant overview with actionable math examples
        return generateOfflineHelpResponse(trimmed)
    }

    private fun isExplanationRequest(query: String): Boolean {
        val q = query.lowercase()
        return q == "explain" || q == "explain this" || q == "explain formula" ||
                q.contains("explain current") || q.contains("how to calculate") ||
                q.contains("break down") || q.contains("what is this")
    }

    private fun explainCalculatorContext(context: String, angleMode: AngleMode): Pair<String, String?> {
        val lines = context.split("\n")
        val expr = lines.firstOrNull { it.startsWith("Expression:") }
            ?.removePrefix("Expression:")?.trim().orEmpty()
        val result = lines.firstOrNull { it.startsWith("Current Result:") }
            ?.removePrefix("Current Result:")?.trim().orEmpty()

        val sb = StringBuilder()
        sb.append("### 📐 Step-by-Step Breakdown\n\n")
        val target = if (expr.isNotBlank()) expr else result
        sb.append("**Target Expression:** `").append(target).append("`\n\n")

        val eval = calcEngine.evaluate(target, angleMode)

        when (eval) {
            is EvaluationResult.Success -> {
                sb.append("#### 1. Analysis of Components\n")
                sb.append("- **Input Expression:** `").append(target).append("`\n")
                sb.append("- **Current Angle Mode:** ").append(angleMode.name).append("\n\n")

                sb.append("#### 2. Order of Operations (PEMDAS/BODMAS)\n")
                if (target.contains("(") && target.contains(")")) {
                    sb.append("1. **Parentheses**: Parenthesized sub-terms are prioritized and resolved first.\n")
                }
                if (target.contains("^") || target.contains("√") || target.contains("sqrt")) {
                    sb.append("2. **Powers & Roots**: Exponents and radical functions are evaluated.\n")
                }
                if (target.contains("*") || target.contains("×") || target.contains("/") || target.contains("÷")) {
                    sb.append("3. **Multiplication & Division**: Evaluated sequentially from left to right.\n")
                }
                if (target.contains("+") || target.contains("-") || target.contains("−")) {
                    sb.append("4. **Addition & Subtraction**: Evaluated last from left to right.\n")
                }

                sb.append("\n#### 3. Calculated Value\n")
                sb.append("Evaluating gives:\n")
                sb.append("```\n")
                sb.append(target).append(" = ").append(eval.formatted).append("\n")
                sb.append("```\n\n")
                sb.append("**RESULT: ").append(eval.formatted).append("**")
                return Pair(sb.toString(), eval.formatted)
            }
            is EvaluationResult.Error -> {
                sb.append("⚠️ The current expression contains a syntax or domain issue: **").append(eval.message).append("**.\n")
                sb.append("- Ensure all open parentheses `(` have matching closed parentheses `)`.\n")
                sb.append("- Check that division by zero or square roots of negative numbers are avoided in real mode.")
                return Pair(sb.toString(), null)
            }
        }
    }

    private fun trySolveQuadratic(query: String): Pair<String, String?>? {
        val q = query.replace(" ", "").lowercase()
        // Matches patterns like ax^2+bx+c=0 or ax²+bx+c=0
        val pattern = Pattern.compile("([+-]?\\d*\\.?\\d*)x(?:\\^2|²)([+-]\\d*\\.?\\d*)x([+-]\\d*\\.?\\d*)=0")
        val matcher = pattern.matcher(q)

        if (!matcher.matches()) {
            // Also test simplified format x^2-5x+6=0
            val pattern2 = Pattern.compile("([+-]?\\d*\\.?\\d*)x(?:\\^2|²)([+-]\\d*\\.?\\d*)x=0")
            val matcher2 = pattern2.matcher(q)
            if (matcher2.matches()) {
                val aStr = matcher2.group(1).let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
                val bStr = matcher2.group(2).let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
                val a = aStr.toDoubleOrNull() ?: 1.0
                val b = bStr.toDoubleOrNull() ?: 0.0
                return formatQuadraticSolution(a, b, 0.0)
            }
            return null
        }

        val aStr = matcher.group(1).let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
        val bStr = matcher.group(2).let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
        val cStr = matcher.group(3)

        val a = aStr.toDoubleOrNull() ?: return null
        val b = bStr.toDoubleOrNull() ?: return null
        val c = cStr.toDoubleOrNull() ?: return null

        return formatQuadraticSolution(a, b, c)
    }

    private fun formatQuadraticSolution(a: Double, b: Double, c: Double): Pair<String, String?> {
        val delta = b * b - 4 * a * c
        val sb = StringBuilder()
        sb.append("### 🔢 Quadratic Equation Solver\n\n")
        sb.append("**Equation:** `${formatNum(a)}x² ${if (b >= 0) "+ " else ""}${formatNum(b)}x ${if (c >= 0) "+ " else ""}${formatNum(c)} = 0`\n\n")

        sb.append("#### 1. Identify Coefficients\n")
        sb.append("- `a = ${formatNum(a)}`\n")
        sb.append("- `b = ${formatNum(b)}`\n")
        sb.append("- `c = ${formatNum(c)}`\n\n")

        sb.append("#### 2. Compute Discriminant (Δ)\n")
        sb.append("Δ = b² - 4ac = (${formatNum(b)})² - 4(${formatNum(a)})(${formatNum(c)})\n")
        sb.append("Δ = ${formatNum(b * b)} - ${formatNum(4 * a * c)} = ${formatNum(delta)}\n\n")

        sb.append("#### 3. Determine Roots\n")
        if (delta > 0) {
            val sqrtDelta = sqrt(delta)
            val x1 = (-b + sqrtDelta) / (2 * a)
            val x2 = (-b - sqrtDelta) / (2 * a)

            sb.append("Since Δ > 0, there are **two distinct real roots**:\n\n")
            sb.append("- **x₁** = `(-(${formatNum(b)}) + ${formatNum(sqrtDelta)}) / (2 * ${formatNum(a)})` = **${formatNum(x1)}**\n")
            sb.append("- **x₂** = `(-(${formatNum(b)}) - ${formatNum(sqrtDelta)}) / (2 * ${formatNum(a)})` = **${formatNum(x2)}**\n\n")
            sb.append("RESULT: ${formatNum(x1)}")
            return Pair(sb.toString(), formatNum(x1))
        } else if (abs(delta) < 1e-12) {
            val x = -b / (2 * a)
            sb.append("Since Δ = 0, there is **one repeated real root**:\n\n")
            sb.append("- **x** = `-b / (2a)` = **${formatNum(x)}**\n\n")
            sb.append("RESULT: ${formatNum(x)}")
            return Pair(sb.toString(), formatNum(x))
        } else {
            val realPart = -b / (2 * a)
            val imagPart = sqrt(-delta) / (2 * abs(a))
            sb.append("Since Δ < 0, the equation has **complex conjugate roots**:\n\n")
            sb.append("- **x₁** = `${formatNum(realPart)} + ${formatNum(imagPart)}i`\n")
            sb.append("- **x₂** = `${formatNum(realPart)} - ${formatNum(imagPart)}i`\n\n")
            sb.append("RESULT: ${formatNum(realPart)} + ${formatNum(imagPart)}i")
            return Pair(sb.toString(), "${formatNum(realPart)} + ${formatNum(imagPart)}i")
        }
    }

    private fun trySolveLinear(query: String): Pair<String, String?>? {
        val q = query.replace(" ", "").lowercase()
        // Matches ax+b=c or ax-b=c or ax=c
        val pattern = Pattern.compile("([+-]?\\d*\\.?\\d*)x([+-]\\d*\\.?\\d*)?=([+-]?\\d*\\.?\\d+)")
        val matcher = pattern.matcher(q)

        if (!matcher.matches()) return null

        val aStr = matcher.group(1).let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
        val bStr = matcher.group(2) ?: "+0"
        val cStr = matcher.group(3) ?: return null

        val a = aStr.toDoubleOrNull() ?: return null
        if (abs(a) < 1e-12) return null
        val b = bStr.toDoubleOrNull() ?: 0.0
        val c = cStr.toDoubleOrNull() ?: return null

        val rhs = c - b
        val x = rhs / a

        val sb = StringBuilder()
        sb.append("### 🔢 Linear Equation Solver\n\n")
        sb.append("**Equation:** `${formatNum(a)}x ${if (b >= 0) "+ " else ""}${formatNum(b)} = ${formatNum(c)}`\n\n")

        sb.append("#### Steps:\n")
        sb.append("1. **Isolate variable term** by moving constant to the right-hand side:\n")
        sb.append("   `${formatNum(a)}x = ${formatNum(c)} - (${formatNum(b)}) = ${formatNum(rhs)}`\n\n")
        sb.append("2. **Divide both sides** by the coefficient of x (`${formatNum(a)}`):\n")
        sb.append("   `x = ${formatNum(rhs)} / ${formatNum(a)} = ${formatNum(x)}`\n\n")
        sb.append("RESULT: ${formatNum(x)}")

        return Pair(sb.toString(), formatNum(x))
    }

    private fun tryPrimeFactors(query: String): Pair<String, String?>? {
        val q = query.lowercase().trim()
        val numMatch = Regex("""(?:prime\s+)?factors?(?:\s+of)?\s+(\d+)""").find(q)
            ?: Regex("""factor\s+(\d+)""").find(q)
            ?: return null

        val n = numMatch.groupValues[1].toLongOrNull() ?: return null
        if (n <= 1 || n > 1000000000L) return null

        var temp = n
        val factors = mutableMapOf<Long, Int>()
        var d = 2L
        while (d * d <= temp) {
            while (temp % d == 0L) {
                factors[d] = (factors[d] ?: 0) + 1
                temp /= d
            }
            d++
        }
        if (temp > 1) {
            factors[temp] = (factors[temp] ?: 0) + 1
        }

        val factorStr = factors.entries.joinToString(" × ") { (base, exp) ->
            if (exp > 1) "$base^$exp" else "$base"
        }

        val sb = StringBuilder()
        sb.append("### 🔢 Prime Factorization of $n\n\n")
        sb.append("The prime factorization expresses `$n` as a product of prime numbers:\n\n")
        sb.append("```\n")
        sb.append("$n = ").append(factorStr).append("\n")
        sb.append("```\n\n")
        sb.append("#### Prime Factors List:\n")
        factors.forEach { (base, exp) ->
            sb.append("- **$base** (multiplicity $exp)\n")
        }
        sb.append("\n**RESULT: $factorStr**")
        return Pair(sb.toString(), factorStr)
    }

    private fun tryGcdLcm(query: String): Pair<String, String?>? {
        val q = query.lowercase().trim()
        val isLcm = q.contains("lcm")
        val match = Regex("""(?:gcd|lcm|hcf)\D*(\d+)\D+(\d+)""").find(q) ?: return null

        val a = match.groupValues[1].toLongOrNull() ?: return null
        val b = match.groupValues[2].toLongOrNull() ?: return null

        val gcdVal = gcd(a, b)
        val lcmVal = if (gcdVal > 0) (a / gcdVal) * b else 0L

        val sb = StringBuilder()
        if (isLcm) {
            sb.append("### 📊 Least Common Multiple (LCM)\n\n")
            sb.append("**Values:** `$a` and `$b`\n\n")
            sb.append("#### Formula:\n")
            sb.append("LCM(a, b) = |a × b| / GCD(a, b)\n\n")
            sb.append("- GCD($a, $b) = **$gcdVal**\n")
            sb.append("- LCM($a, $b) = `($a * $b) / $gcdVal` = **$lcmVal**\n\n")
            sb.append("RESULT: $lcmVal")
            return Pair(sb.toString(), lcmVal.toString())
        } else {
            sb.append("### 📊 Greatest Common Divisor (GCD / HCF)\n\n")
            sb.append("**Values:** `$a` and `$b`\n\n")
            sb.append("#### Euclidean Algorithm Steps:\n")
            var x = a
            var y = b
            while (y != 0L) {
                val r = x % y
                sb.append("- `$x = $y \\times ${x / y} + $r`\n")
                x = y
                y = r
            }
            sb.append("\n**GCD($a, $b) = $gcdVal**\n\n")
            sb.append("RESULT: $gcdVal")
            return Pair(sb.toString(), gcdVal.toString())
        }
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    private fun tryPercentage(query: String): Pair<String, String?>? {
        val q = query.lowercase().trim()
        val match = Regex("""(\d+(?:\.\d+)?)\s*%\s*(?:of)?\s*(\d+(?:\.\d+)?)""").find(q) ?: return null

        val p = match.groupValues[1].toDoubleOrNull() ?: return null
        val total = match.groupValues[2].toDoubleOrNull() ?: return null
        val res = (p / 100.0) * total

        val sb = StringBuilder()
        sb.append("### 📊 Percentage Calculation\n\n")
        sb.append("**Task:** Find `${formatNum(p)}%` of `${formatNum(total)}`\n\n")
        sb.append("#### Formula & Steps:\n")
        sb.append("Amount = (p / 100) × Total\n\n")
        sb.append("Amount = (${formatNum(p)} / 100) × ${formatNum(total)} = ${formatNum(p / 100.0)} × ${formatNum(total)} = **${formatNum(res)}**\n\n")
        sb.append("RESULT: ${formatNum(res)}")
        return Pair(sb.toString(), formatNum(res))
    }

    private fun tryCalculus(query: String): Pair<String, String?>? {
        val q = query.lowercase().trim()
        if (!q.contains("derivative") && !q.contains("d/dx") && !q.contains("integral") && !q.contains("integrate")) {
            return null
        }

        val sb = StringBuilder()
        if (q.contains("integral") || q.contains("integrate")) {
            sb.append("### ∬ Calculus: Integration Reference\n\n")
            sb.append("#### Common Indefinite Integrals:\n")
            sb.append("- **Power Rule:** ∫ xⁿ dx = (xⁿ⁺¹) / (n + 1) + C *(for n ≠ -1)*\n")
            sb.append("- **Reciprocal:** ∫ (1/x) dx = ln|x| + C\n")
            sb.append("- **Exponential:** ∫ eᵏˣ dx = (1/k)eᵏˣ + C\n")
            sb.append("- **Sine:** ∫ sin(x) dx = -cos(x) + C\n")
            sb.append("- **Cosine:** ∫ cos(x) dx = sin(x) + C\n")
            sb.append("- **Secant Squared:** ∫ sec²(x) dx = tan(x) + C\n\n")
            sb.append("#### Integration by Parts Formula:\n")
            sb.append("∫ u dv = u v - ∫ v du\n")
            return Pair(sb.toString(), null)
        } else {
            sb.append("### 𝑑/𝑑𝑥 Calculus: Differentiation Reference\n\n")
            sb.append("#### Core Derivative Rules:\n")
            sb.append("- **Power Rule:** d/dx [xⁿ] = n xⁿ⁻¹\n")
            sb.append("- **Product Rule:** d/dx [u · v] = u'v + uv'\n")
            sb.append("- **Quotient Rule:** d/dx [u / v] = (u'v - uv') / v²\n")
            sb.append("- **Chain Rule:** d/dx [f(g(x))] = f'(g(x)) · g'(x)\n\n")
            sb.append("#### Standard Derivatives:\n")
            sb.append("- d/dx [sin(x)] = cos(x)\n")
            sb.append("- d/dx [cos(x)] = -sin(x)\n")
            sb.append("- d/dx [tan(x)] = sec²(x)\n")
            sb.append("- d/dx [eˣ] = eˣ\n")
            sb.append("- d/dx [ln(x)] = 1/x\n")
            return Pair(sb.toString(), null)
        }
    }

    private fun tryTrigExact(query: String, angleMode: AngleMode): Pair<String, String?>? {
        val q = query.lowercase().trim()
        val trigMatch = Regex("""^(sin|cos|tan)\(?([+-]?\d+(?:\.\d+)?)\)?""").find(q) ?: return null

        val fn = trigMatch.groupValues[1]
        val arg = trigMatch.groupValues[2].toDoubleOrNull() ?: return null

        // Convert arg to degrees for standard angle lookup
        val deg = if (angleMode == AngleMode.RAD) arg * (180.0 / Math.PI) else arg
        val normDeg = ((deg % 360.0) + 360.0) % 360.0

        val (exactStr, numVal) = when (fn) {
            "sin" -> when (normDeg.roundToInt()) {
                0, 180, 360 -> Pair("0", 0.0)
                30, 150 -> Pair("1/2 = 0.5", 0.5)
                45, 135 -> Pair("√2 / 2 ≈ 0.7071", sqrt(2.0) / 2.0)
                60, 120 -> Pair("√3 / 2 ≈ 0.8660", sqrt(3.0) / 2.0)
                90 -> Pair("1", 1.0)
                210, 330 -> Pair("-1/2 = -0.5", -0.5)
                225, 315 -> Pair("-√2 / 2 ≈ -0.7071", -sqrt(2.0) / 2.0)
                240, 300 -> Pair("-√3 / 2 ≈ -0.8660", -sqrt(3.0) / 2.0)
                270 -> Pair("-1", -1.0)
                else -> null
            }
            "cos" -> when (normDeg.roundToInt()) {
                0, 360 -> Pair("1", 1.0)
                30, 330 -> Pair("√3 / 2 ≈ 0.8660", sqrt(3.0) / 2.0)
                45, 315 -> Pair("√2 / 2 ≈ 0.7071", sqrt(2.0) / 2.0)
                60, 300 -> Pair("1/2 = 0.5", 0.5)
                90, 270 -> Pair("0", 0.0)
                120, 240 -> Pair("-1/2 = -0.5", -0.5)
                135, 225 -> Pair("-√2 / 2 ≈ -0.7071", -sqrt(2.0) / 2.0)
                150, 210 -> Pair("-√3 / 2 ≈ -0.8660", -sqrt(3.0) / 2.0)
                180 -> Pair("-1", -1.0)
                else -> null
            }
            "tan" -> when (normDeg.roundToInt()) {
                0, 180, 360 -> Pair("0", 0.0)
                30, 210 -> Pair("1/√3 ≈ 0.5774", 1.0 / sqrt(3.0))
                45, 225 -> Pair("1", 1.0)
                60, 240 -> Pair("√3 ≈ 1.7321", sqrt(3.0))
                90, 270 -> Pair("Undefined (Asymptote)", Double.NaN)
                120, 300 -> Pair("-√3 ≈ -1.7321", -sqrt(3.0))
                135, 315 -> Pair("-1", -1.0)
                150, 330 -> Pair("-1/√3 ≈ -0.5774", -1.0 / sqrt(3.0))
                else -> null
            }
            else -> null
        } ?: return null

        val sb = StringBuilder()
        sb.append("### 📐 Trigonometry Exact Value\n\n")
        sb.append("**Evaluation:** `$fn($arg` ${if (angleMode == AngleMode.RAD) "rad" else "°"}`)\n\n")
        sb.append("- **Angle in Degrees:** `${formatNum(deg)}°`\n")
        sb.append("- **Exact Value:** **$exactStr**\n\n")
        if (!numVal.isNaN()) {
            sb.append("RESULT: ${formatNum(numVal)}")
            return Pair(sb.toString(), formatNum(numVal))
        } else {
            return Pair(sb.toString(), null)
        }
    }

    private fun tryScientificConstant(query: String): Pair<String, String?>? {
        val q = query.lowercase().trim()
        val constants = listOf(
            Triple(listOf("speed of light", "c ", "light speed"), "c = 299,792,458 m/s", "299792458"),
            Triple(listOf("planck", "planck's", "h "), "h = 6.62607015 × 10⁻³⁴ J·s", "6.62607015e-34"),
            Triple(listOf("gravity", "gravitational", "g "), "G = 6.67430 × 10⁻¹¹ N·m²/kg²", "6.6743e-11"),
            Triple(listOf("avogadro", "na"), "N_A = 6.02214076 × 10²³ mol⁻¹", "6.02214076e23"),
            Triple(listOf("boltzmann", "k "), "k = 1.380649 × 10⁻²³ J/K", "1.380649e-23"),
            Triple(listOf("electron charge", "elementary charge", "e charge"), "e = 1.602176634 × 10⁻¹⁹ C", "1.602176634e-19"),
            Triple(listOf("pi", "pi value"), "π = 3.141592653589793", "3.141592653589793"),
            Triple(listOf("euler", "e value", "constant e"), "e = 2.718281828459045", "2.718281828459045")
        )

        for ((keys, info, value) in constants) {
            if (keys.any { q.contains(it) }) {
                val sb = StringBuilder()
                sb.append("### ⚛️ Scientific Constant Reference\n\n")
                sb.append("**Constant:** $info\n\n")
                sb.append("This is an internationally accepted physical standard accessible 100% offline.\n\n")
                sb.append("RESULT: $value")
                return Pair(sb.toString(), value)
            }
        }
        return null
    }

    private fun tryStepByStepEvaluation(
        query: String,
        calculatorContext: String?,
        angleMode: AngleMode
    ): Pair<String, String?>? {
        val cleaned = query
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("what is", "", ignoreCase = true)
            .replace("calculate", "", ignoreCase = true)
            .replace("solve", "", ignoreCase = true)
            .replace("eval", "", ignoreCase = true)
            .trim()

        val eval = calcEngine.evaluate(cleaned, angleMode)
        if (eval is EvaluationResult.Success) {
            val sb = StringBuilder()
            sb.append("### 🧮 Step-by-Step Offline Solution\n\n")
            sb.append("**Problem:** `").append(cleaned).append("`\n\n")

            sb.append("#### Step-by-Step Evaluation:\n")
            if (cleaned.contains("(") && cleaned.contains(")")) {
                sb.append("1. **Resolve Parentheses:** Sub-expressions grouped inside parentheses are computed first.\n")
            }
            if (cleaned.contains("^") || cleaned.contains("√")) {
                sb.append("2. **Evaluate Powers & Radicals:** Exponentials and square roots are simplified.\n")
            }
            if (cleaned.contains("*") || cleaned.contains("/")) {
                sb.append("3. **Multiplication & Division:** Operations computed in sequence from left to right.\n")
            }
            if (cleaned.contains("+") || cleaned.contains("-")) {
                sb.append("4. **Addition & Subtraction:** Final terms combined.\n")
            }

            sb.append("\n```\n")
            sb.append(cleaned).append(" = ").append(eval.formatted).append("\n")
            sb.append("```\n\n")
            sb.append("**RESULT: ").append(eval.formatted).append("**")
            return Pair(sb.toString(), eval.formatted)
        }
        return null
    }

    private fun generateOfflineHelpResponse(query: String): Pair<String, String?> {
        val sb = StringBuilder()
        sb.append("### ⚡ Offline Math Assistant\n\n")
        sb.append("You are in **100% Offline Mode**. All mathematical reasoning, equation solving, and formula breakdowns are processed entirely on your device with **zero internet needed**!\n\n")
        sb.append("#### What you can ask:\n")
        sb.append("- **Arithmetic & Algebra:** `25 * (14 + 6) / 2`, `sqrt(144) + 12^2`\n")
        sb.append("- **Quadratic Equations:** `x^2 - 5x + 6 = 0`, `2x^2 + 4x - 6 = 0`\n")
        sb.append("- **Linear Equations:** `3x + 9 = 24`, `5x - 15 = 0`\n")
        sb.append("- **Prime Factorization:** `factors of 360`, `factor 84`\n")
        sb.append("- **GCD & LCM:** `gcd 24 36`, `lcm 12 18`\n")
        sb.append("- **Percentages:** `15% of 240`, `20% of 85`\n")
        sb.append("- **Exact Trig Angles:** `sin 30`, `cos 45`, `tan 60`\n")
        sb.append("- **Calculus Formulas:** `derivative of sin(x)`, `integral rules`\n")
        sb.append("- **Scientific Constants:** `speed of light`, `planck`, `gravitational`\n")
        sb.append("- **Formula Breakdown:** Tap **'Explain'** above to deconstruct your active calculator calculation.\n")

        return Pair(sb.toString(), null)
    }

    private fun formatNum(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (abs(value) < 1e-12) return "0"
        val df = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))
        df.isGroupingUsed = false
        val formatted = df.format(value)
        return if (formatted == "-0") "0" else formatted
    }
}
