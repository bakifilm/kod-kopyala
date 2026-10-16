package work.baki.kodkopyala

/**
 * Mesaj metninden doğrulama kodunu bulur.
 *
 * Kural: metinde "kod / şifre / code / OTP ..." gibi bir anahtar kelime olmalı.
 * Sonra 4–8 haneli (veya 123-456 biçimindeki) sayılardan anahtar kelimeye
 * en yakın olanı seçilir. Tutar, tarih ve saat gibi sayılar elenir.
 */
object OtpExtractor {

    private val keywordRegex = Regex(
        "(kod|code|şifre|sifre|parola|doğrulama|dogrulama|onay|otp|pin|" +
            "verification|verify|passcode|password|tek kullanımlık|tek kullanimlik)",
        RegexOption.IGNORE_CASE
    )

    // 123-456 / 1234-5678 biçimi
    private val dashedRegex = Regex("""(?<![\d.,/:-])(\d{3,4})-(\d{3,4})(?![\d/:-]|[.,]\d)""")

    // Düz 4–8 hane. Tutar (1.250,00), tarih (16/10/2026), saat ve telefon (+90...) elenir;
    // cümle sonundaki nokta ise sorun değil.
    // "G-123456" gibi harf-tire önekleri kabul edilir, "12-123456" gibi sayı-tire edilmez.
    private val plainRegex = Regex("""(?<![\d.,/:+])(?<!\d-)\d{4,8}(?![\d/:]|[.,]\d)""")

    fun extract(text: String?): String? {
        if (text.isNullOrBlank()) return null

        val keywords = keywordRegex.findAll(text).map { it.range }.toList()
        if (keywords.isEmpty()) return null

        data class Candidate(val code: String, val range: IntRange)

        val candidates = mutableListOf<Candidate>()
        dashedRegex.findAll(text).forEach {
            candidates += Candidate(it.groupValues[1] + it.groupValues[2], it.range)
        }
        plainRegex.findAll(text).forEach { m ->
            // Tireli bir kodun parçasıysa tekrar ekleme
            if (candidates.none { c -> m.range.first in c.range }) {
                candidates += Candidate(m.value, m.range)
            }
        }
        if (candidates.isEmpty()) return null

        return candidates.minByOrNull { c ->
            keywords.minOf { k -> distance(c.range, k) }
        }?.code
    }

    private fun distance(a: IntRange, b: IntRange): Int = when {
        a.last < b.first -> b.first - a.last
        b.last < a.first -> a.first - b.last
        else -> 0
    }
}
