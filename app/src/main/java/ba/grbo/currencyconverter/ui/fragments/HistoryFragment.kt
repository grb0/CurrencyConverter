package ba.grbo.currencyconverter.ui.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ba.grbo.currencyconverter.databinding.FragmentHistoryBinding
import ba.grbo.currencyconverter.util.getMaterialFadeThroughAnimator

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator {
        return getMaterialFadeThroughAnimator(binding.frameLayout, enter)
    }
}