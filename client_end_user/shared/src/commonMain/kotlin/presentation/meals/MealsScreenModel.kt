package presentation.meals

import app.cash.paging.PagingData
import cafe.adriel.voyager.core.model.coroutineScope
import domain.entity.Meal
import domain.usecase.IExploreRestaurantUseCase
import domain.usecase.IManageAuthenticationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import presentation.base.BaseScreenModel
import presentation.base.ErrorState
import presentation.resturantDetails.MealInteractionListener
import presentation.resturantDetails.MealUIState
import presentation.resturantDetails.toUIState

class MealsScreenModel(
    private val cuisineId: String,
    private val cuisineName: String,
    private val manageRestaurant: IExploreRestaurantUseCase,
    private val manageAuthentication: IManageAuthenticationUseCase,
) : BaseScreenModel<MealsUiState, MealsUiEffect>(MealsUiState()), MealsInteractionListener,
    MealInteractionListener {

    override val viewModelScope: CoroutineScope = coroutineScope

    init {
        getData()
        checkLoginStatus()
    }

    private fun getData() {
        updateState { it.copy(isLoading = true, cuisineName = cuisineName) }
        tryToExecute(
            { manageRestaurant.getMealsInCuisine(cuisineId) },
            ::onGetMealsSuccess,
            ::onError
        )
    }

    private fun checkLoginStatus() {
        tryToExecute(
            { manageAuthentication.getAccessToken() },
            ::onCheckLoginSuccess,
            ::onError
        )
    }

    private fun onCheckLoginSuccess(accessToken: Flow<String>) {
        coroutineScope.launch {
            accessToken.collect { token ->
                if (token.isNotEmpty()) {
                    updateState { it.copy(isLogin = true) }
                } else {
                    updateState { it.copy(isLogin = false) }
                }
            }
        }
    }

    private fun onError(errorState: ErrorState) {
        println("errorState: $errorState")
        updateState { it.copy(isLoading = false) }
        when (errorState) {
            is ErrorState.NoInternet -> {
                updateState { it.copy(error = errorState) }
            }

            else -> {
                updateState { it.copy(error = errorState) }
            }
        }
    }

    private fun onGetMealsSuccess(meals: Flow<PagingData<Meal>>) {
        updateState { it.copy(meals = meals.toUIState()) }
    }

    override fun onIncreaseMealQuantity() {
        val quality = state.value.selectedMeal.quantity + 1
        updateState {
            it.copy(
                selectedMeal = state.value.selectedMeal.copy(
                    quantity = quality,
                    totalPrice = state.value.selectedMeal.price * quality
                )
            )
        }
    }

    override fun onDecreaseMealQuantity() {
        if (state.value.selectedMeal.quantity == 1) return
        updateState {
            val quality = state.value.selectedMeal.quantity - 1
            it.copy(
                selectedMeal = state.value.selectedMeal.copy(
                    quantity = quality, totalPrice = state.value.selectedMeal.price * quality
                )
            )
        }
    }

    override fun onAddToCart() {
        if (state.value.isLogin) {
            onDismissSheet()
            //TODO call add to cart
        } else {
            updateState { it.copy(showMealSheet = false, showLoginSheet = true) }
        }
    }

    override fun onLoginClicked() {
        onDismissSheet()
        sendNewEffect(MealsUiEffect.NavigateToLogin)
    }

    override fun onMealClicked(meal: MealUIState) {
        if (!state.value.showMealSheet) {
            updateState { it.copy(showMealSheet = true, selectedMeal = meal) }
        }
    }

    override fun onDismissSheet() {
        updateState { it.copy(showLoginSheet = false, showMealSheet = false) }
    }

    override fun onBackClicked() {
        sendNewEffect(MealsUiEffect.NavigateBack)
    }

}
