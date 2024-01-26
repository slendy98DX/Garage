package com.example.garage.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.garage.BaseApplication
import com.example.garage.adapter.LogoAdapter
import com.example.garage.database.models.Logo
import com.example.garage.databinding.FragmentLogoListBinding
import com.example.garage.viewmodel.CarViewModel
import com.example.garage.viewmodel.CarViewModelFactory

class LogoListFragment : Fragment() {

    private val viewModel: CarViewModel by activityViewModels {
        CarViewModelFactory(
            requireActivity().application,
            (requireActivity().application as BaseApplication).database.carDao()
        )
    }

    private var _binding: FragmentLogoListBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var logoAdapter: LogoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogoListBinding.inflate(inflater, container, false)
        val view = binding.root

        val recyclerView: RecyclerView = binding.logoRecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        logoAdapter = LogoAdapter(viewModel.logos.value) { clickedLogo ->
            onLogoClicked(clickedLogo)
        }
        recyclerView.adapter = logoAdapter

        searchView = binding.searchBar
        setupSearchView()

        viewModel.logos.observe(viewLifecycleOwner) { logos ->
            logoAdapter.updateLogos(logos)
        }

        return view
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                logoAdapter.filter(newText.orEmpty())
                return true
            }
        })
    }

    private fun onLogoClicked(clickedLogo: Logo) {
        viewModel.setSelectedBrand(clickedLogo.name)
        viewModel.setSelectedLogo(clickedLogo.image.source)

        val action = LogoListFragmentDirections.actionLogoListFragmentToAddEditCarFragment(0)

        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
