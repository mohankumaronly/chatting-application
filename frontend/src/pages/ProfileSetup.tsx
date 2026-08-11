// src/pages/ProfileSetup.tsx
import React, { useState, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useTheme } from '../context/ThemeContext'
import Lottie from 'react-lottie-player'
import profileAnimation from '../assets/lottie/create-profile.json'
import { 
  UserIcon,
  CameraIcon,
  ArrowRightIcon,
  SunIcon,
  MoonIcon,
  XMarkIcon,
  ExclamationCircleIcon,
  PhotoIcon
} from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'

const ProfileSetup = () => {
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [avatar, setAvatar] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    displayName: '',
    bio: '',
    status: 'online'
  })
  const [errors, setErrors] = useState<{ displayName?: string }>({})

  const handleAvatarUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onloadend = () => {
        setAvatar(reader.result as string)
      }
      reader.readAsDataURL(file)
    }
  }

  const removeAvatar = () => {
    setAvatar(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const validateForm = () => {
    const newErrors: { displayName?: string } = {}
    if (!formData.displayName) {
      newErrors.displayName = 'Display name is required'
    } else if (formData.displayName.length < 2) {
      newErrors.displayName = 'Display name must be at least 2 characters'
    }
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!validateForm()) return

    setIsLoading(true)
    try {
      await new Promise(resolve => setTimeout(resolve, 1500))
      toast.success('Profile setup complete!')
      navigate('/app')
    } catch (error) {
      toast.error('Failed to setup profile. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }))
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50/50 to-white dark:from-[#18191A] dark:to-[#1F2022] flex flex-col justify-center py-8 sm:py-12 px-4 sm:px-6 lg:px-8">
      {/* Theme Toggle */}
      <div className="absolute top-4 right-4 sm:top-6 sm:right-6">
        <button
          onClick={toggleTheme}
          className="p-2 rounded-full hover:bg-gray-200 dark:hover:bg-[#3A3B3C] transition-colors"
          aria-label="Toggle theme"
        >
          {theme === 'dark' ? (
            <SunIcon className="w-5 h-5 text-yellow-400" />
          ) : (
            <MoonIcon className="w-5 h-5 text-gray-700" />
          )}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 max-w-5xl mx-auto w-full">
        {/* Left Column - Animation */}
        <div className="hidden lg:flex items-center justify-center">
          <div className="w-full max-w-md">
            <Lottie
              loop
              play
              animationData={profileAnimation}
              style={{ 
                width: '100%', 
                height: 'auto',
                display: 'block'
              }}
            />
          </div>
        </div>

        {/* Right Column - Form */}
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="flex justify-center lg:hidden">
            <div className="w-14 h-14 sm:w-16 sm:h-16 bg-gradient-to-r from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <UserIcon className="w-8 h-8 sm:w-10 sm:h-10 text-white" />
            </div>
          </div>
          <h2 className="mt-4 sm:mt-6 text-center text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white">
            Set Up Your Profile
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
            Tell others a bit about yourself
          </p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mt-6 sm:mt-8 bg-white dark:bg-[#242526] py-8 px-6 sm:px-10 shadow-xl rounded-2xl border border-gray-100 dark:border-[#3E4042]"
          >
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Avatar Upload */}
              <div className="flex flex-col items-center">
                <div className="relative">
                  <div 
                    className={`w-24 h-24 sm:w-28 sm:h-28 rounded-full flex items-center justify-center overflow-hidden ${
                      avatar ? '' : 'bg-gray-200 dark:bg-[#3A3B3C] border-2 border-dashed border-gray-300 dark:border-[#3E4042]'
                    }`}
                  >
                    {avatar ? (
                      <img src={avatar} alt="Avatar" className="w-full h-full object-cover" />
                    ) : (
                      <PhotoIcon className="w-10 h-10 text-gray-400 dark:text-[#B0B3B8]" />
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="absolute bottom-0 right-0 p-2 bg-blue-500 rounded-full shadow-lg hover:bg-blue-600 transition-colors"
                  >
                    <CameraIcon className="w-4 h-4 text-white" />
                  </button>
                  {avatar && (
                    <button
                      type="button"
                      onClick={removeAvatar}
                      className="absolute top-0 right-0 p-1 bg-red-500 rounded-full shadow-lg hover:bg-red-600 transition-colors"
                    >
                      <XMarkIcon className="w-3 h-3 text-white" />
                    </button>
                  )}
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarUpload}
                    className="hidden"
                  />
                </div>
                <p className="mt-2 text-xs text-gray-500 dark:text-[#B0B3B8]">
                  Click the camera icon to upload
                </p>
              </div>

              {/* Display Name */}
              <div>
                <label htmlFor="displayName" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                  Display Name
                </label>
                <input
                  id="displayName"
                  name="displayName"
                  type="text"
                  value={formData.displayName}
                  onChange={handleChange}
                  className={`app-input block w-full px-4 py-2.5 rounded-xl text-sm mt-1 ${
                    errors.displayName ? 'border-red-500 focus:ring-red-500' : ''
                  }`}
                  placeholder="Your display name"
                />
                {errors.displayName && (
                  <p className="mt-1 text-sm text-red-500 flex items-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {errors.displayName}
                  </p>
                )}
              </div>

              {/* Bio */}
              <div>
                <label htmlFor="bio" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                  Bio <span className="text-gray-400 dark:text-[#B0B3B8]">(optional)</span>
                </label>
                <textarea
                  id="bio"
                  name="bio"
                  rows={3}
                  value={formData.bio}
                  onChange={handleChange}
                  className="app-input block w-full px-4 py-2.5 rounded-xl text-sm mt-1 resize-none"
                  placeholder="Tell others about yourself..."
                  maxLength={150}
                />
                <p className="mt-1 text-xs text-gray-500 dark:text-[#B0B3B8] text-right">
                  {formData.bio.length}/150
                </p>
              </div>

              {/* Status */}
              <div>
                <label htmlFor="status" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                  Status
                </label>
                <select
                  id="status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  className="app-input block w-full px-4 py-2.5 rounded-xl text-sm mt-1"
                >
                  <option value="online">Online</option>
                  <option value="away">Away</option>
                  <option value="busy">Busy</option>
                  <option value="offline">Offline</option>
                </select>
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isLoading}
                className="btn-primary w-full flex justify-center items-center py-2.5 px-4 rounded-xl text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 transition-all shadow-sm hover:shadow-md disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    Setting up...
                  </>
                ) : (
                  <>
                    Complete Setup
                    <ArrowRightIcon className="w-4 h-4 ml-2" />
                  </>
                )}
              </button>

              {/* Skip for now */}
              <p className="text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
                <Link to="/app" className="text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  Skip for now
                </Link>
              </p>
            </form>
          </motion.div>
        </div>
      </div>
    </div>
  )
}

export default ProfileSetup