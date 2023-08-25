package org.thechance.common.presentation.login

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beepbeep.designSystem.ui.composable.BpButton
import com.beepbeep.designSystem.ui.composable.BpCheckBox
import com.beepbeep.designSystem.ui.composable.BpTextField
import com.beepbeep.designSystem.ui.theme.Theme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.thechance.common.presentation.composables.BpLogo
import org.thechance.common.presentation.main.MainContainer
import org.thechance.common.presentation.resources.Resources
import org.thechance.common.presentation.uistate.LoginUiState


object LoginScreen : Screen, KoinComponent {

    private val screenModel: LoginScreenModel by inject()

    @Composable
    override fun Content() {
        val navigate = LocalNavigator.currentOrThrow
        val state by screenModel.state.collectAsState()


        LoginContent(
            state = state,
            onClickLogin = { navigate.push(MainContainer) },
            onUserNameChanged = screenModel::onUsernameChange,
            onPasswordChanged = screenModel::onPasswordChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    state: LoginUiState,
    onClickLogin: () -> Unit,
    onUserNameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
) {
    Row(
        Modifier.background(Theme.colors.surface).fillMaxSize()
            .padding(
                top = Theme.dimens.space40,
                start = Theme.dimens.space40,
                bottom = Theme.dimens.space40
            ),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(Modifier.weight(1f)) {
            Image(
                painter = painterResource(
                    if (isSystemInDarkTheme()) Resources.Strings.loginImageDark else Resources.Strings.loginImageLight
                ),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
                    .border(
                        BorderStroke(width = 1.dp, color = Theme.colors.divider),
                        shape = RoundedCornerShape(Theme.radius.large)
                    )
                    .clip(RoundedCornerShape(Theme.radius.large))
            )
            BpLogo(
                expanded = true,
                modifier = Modifier.align(Alignment.TopStart).padding(Theme.dimens.space32)
            )
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxHeight().width(350.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    Resources.Strings.login,
                    style = Theme.typography.headlineLarge,
                    color = Theme.colors.contentPrimary
                )
                Text(
                    Resources.Strings.loginTitle,
                    style = Theme.typography.titleMedium,
                    color = Theme.colors.contentTertiary,
                    modifier = Modifier.padding(top = Theme.dimens.space8)
                )
                BpTextField(
                    onValueChange = onUserNameChanged,
                    text = state.username,
                    label = Resources.Strings.loginUsername,
                    modifier = Modifier.padding(top = Theme.dimens.space40),
                    hint = ""
                )
                BpTextField(
                    onValueChange = onPasswordChanged,
                    text = state.password,
                    label = Resources.Strings.loginPassword,
                    keyboardType = KeyboardType.Password,
                    modifier = Modifier.padding(top = Theme.dimens.space16),
                    hint = ""
                )
                BpCheckBox(
                    label = Resources.Strings.loginKeepMeLoggedIn,
                    isChecked = false,
                    onCheck = {},
                    modifier = Modifier.fillMaxWidth().padding(top = Theme.dimens.space16)
                )
                BpButton(
                    title = Resources.Strings.loginButton,
                    onClick = onClickLogin,
                    modifier = Modifier.padding(top = Theme.dimens.space24).fillMaxWidth()
                )
            }
        }
    }
}