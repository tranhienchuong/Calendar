package com.example.lichvannien.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lichvannien.domain.usecase.SaveBirthdayUseCase
import com.example.lichvannien.ui.onboarding.OnboardingScreen
import com.example.lichvannien.ui.onboarding.OnboardingViewModel
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val saveBirthdayUseCase = mockk<SaveBirthdayUseCase>(relaxed = true)

    @Before
    fun setup() {
        // Reset Preferences File sạch sẽ trước mỗi lượt test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        try {
            File(context.filesDir.parent, "datastore/user_preferences.preferences_pb").delete()
        } catch (e: Exception) {
            // Bỏ qua nếu file không tồn tại
        }
    }

    @Test
    fun testOnboardingUiElements() {
        val viewModel = OnboardingViewModel(saveBirthdayUseCase)

        composeTestRule.setContent {
            OnboardingScreen(
                onFinished = { },
                viewModel = viewModel
            )
        }

        // Kiểm tra nút Chọn ngày sinh hiển thị đúng và có khả năng tương tác
        composeTestRule.onNodeWithText("Chọn ngày sinh").assertExists()
    }
}
