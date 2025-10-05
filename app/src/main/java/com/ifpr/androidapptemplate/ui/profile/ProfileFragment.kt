package com.ifpr.androidapptemplate.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentProfileBinding
import com.ifpr.androidapptemplate.data.model.Usuario

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    
    // Request code for image selection
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        setupUI()
        observeViewModel()
        
        return root
    }
    
    private fun setupUI() {
        binding.apply {
            // Set up profile image click listener
            imageViewProfile.setOnClickListener {
                openImagePicker()
            }
            
            // Set up save button
            buttonSave.setOnClickListener {
                saveProfile()
            }
            
            // Load default profile image
            loadProfileImage("")
        }
    }
    
    private fun observeViewModel() {
        profileViewModel.currentUser.observe(viewLifecycleOwner) { usuario ->
            updateUIWithUserData(usuario)
        }
        
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                profileViewModel.clearError()
            }
        }
        
        profileViewModel.successMessage.observe(viewLifecycleOwner) { successMessage ->
            successMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearSuccess()
            }
        }
    }
    
    private fun updateUIWithUserData(usuario: Usuario) {
        binding.apply {
            editTextName.setText(usuario.nome)
            editTextEmail.setText(usuario.email)
            editTextBio.setText(usuario.biografia)
            editTextLocation.setText(usuario.localizacao)
            loadProfileImage(usuario.avatarUrl)
        }
    }
    
    private fun loadProfileImage(imageUrl: String) {
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person_24dp)
                .error(R.drawable.ic_person_24dp)
                .transform(CircleCrop())
                .into(binding.imageViewProfile)
        } else {
            binding.imageViewProfile.setImageResource(R.drawable.ic_person_24dp)
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PICK_IMAGE_REQUEST)
    }
    
    private fun saveProfile() {
        val nome = binding.editTextName.text.toString()
        val bio = binding.editTextBio.text.toString()
        val location = binding.editTextLocation.text.toString()
        
        profileViewModel.updateProfile(nome, bio, location)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            imageUri?.let {
                // Load selected image
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_person_24dp)
                    .error(R.drawable.ic_person_24dp)
                    .transform(CircleCrop())
                    .into(binding.imageViewProfile)
                
                // Upload image to server (in a real app)
                profileViewModel.uploadProfileImage(it)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}