package com.example.garage.ui

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.garage.BaseApplication
import com.example.garage.R
import com.example.garage.adapter.CarAdapter
import com.example.garage.adapter.CarListener
import com.example.garage.databinding.FragmentCarListBinding
import com.example.garage.viewmodel.CarViewModel
import com.example.garage.viewmodel.CarViewModelFactory
import kotlinx.coroutines.launch

class CarListFragment : Fragment() {

    private val viewModel: CarViewModel by activityViewModels {
        CarViewModelFactory(requireActivity().application,
            (requireActivity().application as BaseApplication).database.carDao())
    }

    private var _binding: FragmentCarListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCarListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slidingPaneLayout = binding.slidingPaneLayout

        slidingPaneLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED

        val onBackPressedCallback = CarListOnBackPressedCallback(binding.slidingPaneLayout)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        val adapter = CarAdapter(CarListener { car ->
            viewModel.setCar(car)
            if(!viewModel.logos.value.isNullOrEmpty()) {
                viewModel.setSelectedLogo(viewModel.logos.value?.filter { it.name == car.brand }!![0].image.source)
            } else {
                viewModel.setSelectedLogo("")
            }
            binding.detailContainer.visibility = View.VISIBLE
            binding.slidingPaneLayout.openPane()

        })

        if(resources.configuration.screenWidthDp > 900 || resources.configuration.screenHeightDp > 900) {
            binding.detailContainer.visibility = View.INVISIBLE
        }

        binding.recyclerView.adapter = adapter
        viewModel.logos.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                viewModel.collectCars().collect{  carsList ->
                    adapter.submitList(carsList)
                    if((resources.configuration.screenWidthDp >= 900 || resources.configuration.screenHeightDp >= 900) && carsList.isNotEmpty()) {
                        binding.recyclerView.post {
                            binding.recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
                        }
                    }
                }
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.addCar.setOnClickListener {
            viewModel.setYear(Calendar.getInstance().get(Calendar.YEAR))
            findNavController().navigate(R.id.action_carListFragment_to_logoListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CarListOnBackPressedCallback(
        private val slidingPaneLayout: SlidingPaneLayout
    ) : OnBackPressedCallback(
        true
    ), SlidingPaneLayout.PanelSlideListener {

        init {
            slidingPaneLayout.addPanelSlideListener(this)
        }

        override fun handleOnBackPressed() {
            slidingPaneLayout.closePane()
        }

        override fun onPanelSlide(panel: View, slideOffset: Float) {}

        override fun onPanelOpened(panel: View) {
            isEnabled = true
        }

        override fun onPanelClosed(panel: View) {
            isEnabled = false
        }
    }
}
