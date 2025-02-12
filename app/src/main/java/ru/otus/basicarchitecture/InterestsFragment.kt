package ru.otus.basicarchitecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.databinding.FragmentInterestsBinding

@AndroidEntryPoint
class InterestsFragment : Fragment() {
    private val viewModel: InterestsViewModel by viewModels()
    private var _binding: FragmentInterestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var flexboxLayoutInterests: FlexboxLayout
    private val _interests = listOf(
        "Котлин", "Андроид", "ML", "Игры", "Фитнес", "Коньки", "Футбол", "Сноуборд",
        "Горные лыжи", "Беговые лыжи", "Музыка", "Фильмы", "Технологии",
        "Киберспорт", "Настольные игры", "Книги", "Фотография",
        "Велосипед", "Путешествия", "Автомобили", "Гаджеты", "Наука", "Кулинария", "Шахматы",
        "Настольный теннис", "Пейнтбол", "Бег", "Йога", "История"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInterestsBinding.inflate(
            inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flexboxLayoutInterests = view.findViewById(R.id.fl_interests)

        // Подписка на изменения списка интересов
        lifecycleScope.launch {
            viewModel.interests.collect { selectedInterests ->
                updateTags(selectedInterests)
            }
        }

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_interestsFragment_to_summaryFragment)
        }
    }

    private fun updateTags(selectedInterests: List<String>) {
        flexboxLayoutInterests.removeAllViews()
        for (interest in _interests) {
            val chip = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_chip, flexboxLayoutInterests, false) as TextView
            chip.text = interest
            chip.isSelected = selectedInterests.contains(interest)

            chip.setOnClickListener {
                chip.isSelected = !chip.isSelected
                viewModel.toggleInterest(interest)
            }

            flexboxLayoutInterests.addView(chip)
        }
    }
}
