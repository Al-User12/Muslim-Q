package com.dapascript.muslimq.presentation.view.detail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dapascript.muslimq.R
import com.dapascript.muslimq.databinding.FragmentQuranDetailBinding
import com.dapascript.muslimq.presentation.BaseActivity
import com.dapascript.muslimq.presentation.adapter.QuranDetailAdapter
import com.dapascript.muslimq.presentation.viewmodel.QuranDetailViewModel
import com.dapascript.muslimq.utils.Resource
import com.dapascript.muslimq.utils.isOnline
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class QuranDetailFragment : Fragment() {

    private var fontSize: Int? = null
    private var _binding: FragmentQuranDetailBinding? = null
    private val binding get() = _binding!!
    private val detailViewModel: QuranDetailViewModel by viewModels()

    private lateinit var detailAdapter: QuranDetailAdapter
    private lateinit var sbCurrent: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
        setViewModel()

        val surahName = arguments?.getString("surahName")

        binding.apply {
            toolbar.title = surahName
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

            ivDescSurah.setOnClickListener {
                showDescSurah()
            }

            ivFontSetting.setOnClickListener {
                showFontSettingDialog()
            }
        }
    }

    private fun showDescSurah() {
        val surahDesc = arguments?.getString("surahDesc")
        val htmlFormat = HtmlCompat.fromHtml(surahDesc.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Deskripsi Surah")
        builder.setMessage(htmlFormat)
        builder.setPositiveButton("Selesai") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    @SuppressLint("NewApi", "InflateParams")
    private fun showFontSettingDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        val inflaterDialog = requireActivity().layoutInflater
        val dialogLayout = inflaterDialog.inflate(R.layout.dialog_font_setting, null)
        val seekBar = dialogLayout.findViewById<SeekBar>(R.id.seekbar_font_size)
        sbCurrent = seekBar!!
        with(builder) {
            setTitle("Atur ukuran ayat")
            sbCurrent.max = 32
            sbCurrent.min = 16
            sbCurrent.progress = fontSize ?: 22
            sbCurrent.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    fontSize = progress
                    sbCurrent.progress = fontSize!!
                    detailAdapter.setFontSize(progress)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun setAdapter() {
        detailAdapter = QuranDetailAdapter()
        binding.rvAyah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailAdapter
            setHasFixedSize(true)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setViewModel() {
        val id = arguments?.getInt("surahNumber")
        id?.let { idSurah ->
            detailViewModel.getQuranDetail(idSurah).observe(viewLifecycleOwner) { data ->
                with(binding) {
                    srlSurah.apply {
                        setLottieAnimation("loading.json")
                        setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                        setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                        setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                            override fun onRefresh() {
                                val handlerData = Handler(Looper.getMainLooper())
                                val check = isOnline(requireContext())
                                if (check) {
                                    handlerData.postDelayed({
                                        setRefreshing(false)
                                    }, 2000)

                                    handlerData.postDelayed({
                                        if (data.data == null) {
                                            setViewModel()
                                            setAdapter()
                                            clNoInternet.visibility = View.GONE
                                        } else {
                                            clSurah.visibility = View.VISIBLE
                                            rvAyah.visibility = View.VISIBLE
                                            clNoInternet.visibility = View.GONE
                                        }
                                    }, 2350)
                                } else {
                                    clSurah.visibility = View.GONE
                                    rvAyah.visibility = View.GONE
                                    clNoInternet.visibility = View.VISIBLE
                                    setRefreshing(false)
                                }
                            }
                        })
                    }
                }

                when {
                    data is Resource.Loading && data.data == null -> {
                        stateLoading(true)
                    }
                    data is Resource.Error && data.data == null -> {
                        stateLoading(false)
                        binding.clNoInternet.visibility = View.VISIBLE
                        binding.clSurah.visibility = View.GONE
                    }
                    else -> {
                        binding.apply {
                            stateLoading(false)
                            clNoInternet.visibility = View.GONE
                            rvAyah.visibility = View.VISIBLE
                            tvSurahName.text = data.data?.namaLatin
                            tvAyahMeaning.text = data.data?.artiQuran
                            tvCityAndTotalAyah.text =
                                "${
                                    data.data?.tempatTurun?.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            Locale.getDefault()
                                        ) else it.toString()
                                    }
                                } • ${data.data?.jumlahAyat} ayat"

                            detailAdapter.setList(data.data?.ayat!!)
                        }
                    }
                }
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        binding.apply {
            if (state) {
                progressBar.visibility = View.VISIBLE
                progressHeader.visibility = View.VISIBLE
                clSurah.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                progressHeader.visibility = View.GONE
                clSurah.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        fontSize = sharedPref.getInt("fontSize", 22)
        detailAdapter.setFontSize(fontSize!!)
    }

    override fun onPause() {
        super.onPause()

        fontSize?.let {
            val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("fontSize", it)
                apply()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as BaseActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as BaseActivity).showBottomNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}