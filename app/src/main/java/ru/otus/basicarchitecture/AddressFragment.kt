package ru.otus.basicarchitecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.databinding.FragmentAddressBinding

@AndroidEntryPoint
class AddressFragment : Fragment() {
    private val viewModel: AddressViewModel by viewModels()
    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressBinding.inflate(
            inflater, container, false
        )

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Восстанавливаем сохраненные данные
        binding.countryInput.setText(viewModel.country.value)
        binding.cityInput.setText(viewModel.city.value)
        binding.addressInput.setText(viewModel.address.value)

        // Пример списка стран для автозаполнения
        val countryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.countryInput.setAdapter(countryAdapter)

        // Наблюдаем за обновлением списка стран
        lifecycleScope.launch {
            viewModel.countrySuggestions.collectLatest { suggestions ->
                countryAdapter.clear()
                countryAdapter.addAll(suggestions)
                countryAdapter.notifyDataSetChanged()
            }
        }

        binding.countryInput.doAfterTextChanged { text ->
            try {
                viewModel.updateCountry(text.toString())
                viewModel.loadCountries(text.toString()) // Загружаем список стран при изменении текста
            } catch (ex: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.load_counties, ex.message), Toast.LENGTH_SHORT
                ).show()
            }
        }


        val cityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.cityInput.setAdapter(cityAdapter)

        // Наблюдаем за обновлением списка стран
        lifecycleScope.launch {
            viewModel.citySuggestions.collectLatest { suggestions ->
                cityAdapter.clear()
                cityAdapter.addAll(suggestions.map { it.location?.value })
                cityAdapter.notifyDataSetChanged()
            }
        }

        binding.cityInput.doAfterTextChanged { text ->
            try {
                viewModel.updateCity(text.toString())
                viewModel.loadCities("${viewModel.country.value}, ${text.toString()}")
            } catch (ex: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.load_cities, ex.message), Toast.LENGTH_SHORT
                ).show()
            }
        }

        val addressAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.addressInput.setAdapter(addressAdapter)

        // Наблюдаем за обновлением списка стран
        lifecycleScope.launch {
            viewModel.addressSuggestions.collectLatest { suggestions ->
                addressAdapter.clear()
                addressAdapter.addAll(suggestions)
                addressAdapter.notifyDataSetChanged()

                updateNextButton()

            }
        }

        binding.addressInput.doAfterTextChanged { text ->
            try {
                viewModel.updateAddress(text.toString())
                updateNextButton()
                viewModel.loadAddressSuggestions("${viewModel.country.value}, ${viewModel.city.value}, ${text.toString()}")

            } catch (ex: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.load_address, ex.message), Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.nextButton.setOnClickListener {
            viewModel.country.value = binding.countryInput.text.toString()
            viewModel.city.value = binding.cityInput.text.toString()
            viewModel.address.value = binding.addressInput.text.toString()
            findNavController().navigate(R.id.action_addressFragment_to_interestsFragment)
        }


        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.loading.visibility = View.VISIBLE
                    }

                    is UiState.Success, is UiState.Idle -> {
                        binding.loading.visibility = View.GONE
                    }

                    is UiState.Error -> {
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        updateNextButton()

        lifecycleScope.launch {
            viewModel.country.collectLatest { country ->
                if (!country.equals(binding.countryInput.text.toString())) {
                    binding.countryInput.setText(country)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.city.collectLatest { city ->
                if (!city.equals(binding.cityInput.text.toString())) {
                    binding.cityInput.setText(city)
                }
            }
        }

        if (viewModel.country.value.isBlank() || viewModel.city.value.isBlank()) {
            lifecycleScope.launch {

                viewModel.loadCityByIp()
            }
        }
    }

    private fun updateNextButton() {


        binding.nextButton.isEnabled =
            binding.addressInput.text.toString().isNotBlank()
                    && (viewModel.addressSuggestions.value.size == 1
                    || cleanString(viewModel.country.value).equals(
                cleanString(viewModel.confirmedAddress.value.country), ignoreCase = true
            )
                    && cleanString(viewModel.city.value).equals(
                cleanString(viewModel.confirmedAddress.value.city), ignoreCase = true
            )
                    )
                    && (viewModel.addressSuggestions.value.size == 1
                    && cleanString(binding.addressInput.text.toString()).equals(
                cleanString(viewModel.addressSuggestions.value[0]), ignoreCase = true
            )
                    || cleanString(binding.addressInput.text.toString()).equals(
                cleanString(viewModel.confirmedAddress.value.streetWithHouseAndFlat),
                ignoreCase = true
            )
                    )
                    || (getString(R.string.back_door) == binding.addressInput.text.toString())
    }

    fun cleanString(input: String): String {
        // Убираем все пробелы
        val trimmed = input.replace(Regex("\\s+"), "")
        // Находим первую и последнюю букву (русскую/латинскую) или цифру
        val match = Regex("([a-zA-Zа-яА-Я0-9]).*?([a-zA-Zа-яА-Я0-9])").find(trimmed)
        return match?.value ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}