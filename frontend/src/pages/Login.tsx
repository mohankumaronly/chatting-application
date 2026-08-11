// src/pages/Login.tsx
import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useTheme } from '../context/ThemeContext'
import Lottie from 'react-lottie-player'
import loginAnimation from '../assets/lottie/Login.json'
import { 
  EnvelopeIcon, 
  LockClosedIcon,
  EyeIcon,
  EyeSlashIcon,
  ArrowRightIcon,
  SunIcon,
  MoonIcon,
  ExclamationCircleIcon
} from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'

const Login = () => {
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false
  })
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({})

  const validateForm = () => {
    const newErrors: { email?: string; password?: string } = {}
    if (!formData.email) {
      newErrors.email = 'Email is required'
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email'
    }
    if (!formData.password) {
      newErrors.password = 'Password is required'
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
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
      toast.success('Login successful!')
      navigate('/verify-otp')
    } catch (error) {
      toast.error('Login failed. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }))
    }
  }

  // Handle forgot password click with navigation
  const handleForgotPassword = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault()
    navigate('/forgot-password')
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50/50 to-white dark:from-[#18191A] dark:to-[#1F2022] flex flex-col justify-center py-8 sm:py-12 px-4 sm:px-6 lg:px-8">
      {/* Theme Toggle - Top Right */}
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
              animationData={loginAnimation}
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
            <div className="w-12 h-12 sm:w-14 sm:h-14 bg-gradient-to-r from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <span className="text-white font-bold text-xl sm:text-2xl">W</span>
            </div>
          </div>
          <h2 className="mt-4 sm:mt-6 text-center text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white">
            Welcome Back
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
            Sign in to continue to your account
          </p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mt-6 sm:mt-8 bg-white dark:bg-[#242526] py-8 px-6 sm:px-10 shadow-xl rounded-2xl border border-gray-100 dark:border-[#3E4042]"
          >
            <form className="space-y-6" onSubmit={handleSubmit}>
              {/* Email Field */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                  Email Address
                </label>
                <div className="mt-1 relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <EnvelopeIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8]" />
                  </div>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    value={formData.email}
                    onChange={handleChange}
                    className={`app-input block w-full pl-10 pr-3 py-2.5 rounded-xl text-sm ${
                      errors.email ? 'border-red-500 focus:ring-red-500' : ''
                    }`}
                    placeholder="you@example.com"
                  />
                </div>
                {errors.email && (
                  <p className="mt-1 text-sm text-red-500 flex items-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {errors.email}
                  </p>
                )}
              </div>

              {/* Password Field */}
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                  Password
                </label>
                <div className="mt-1 relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <LockClosedIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8]" />
                  </div>
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    value={formData.password}
                    onChange={handleChange}
                    className={`app-input block w-full pl-10 pr-10 py-2.5 rounded-xl text-sm ${
                      errors.password ? 'border-red-500 focus:ring-red-500' : ''
                    }`}
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  >
                    {showPassword ? (
                      <EyeSlashIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8] hover:text-gray-600 dark:hover:text-[#E4E6EB] transition-colors" />
                    ) : (
                      <EyeIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8] hover:text-gray-600 dark:hover:text-[#E4E6EB] transition-colors" />
                    )}
                  </button>
                </div>
                {errors.password && (
                  <p className="mt-1 text-sm text-red-500 flex items-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {errors.password}
                  </p>
                )}
              </div>

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <input
                    id="rememberMe"
                    name="rememberMe"
                    type="checkbox"
                    checked={formData.rememberMe}
                    onChange={handleChange}
                    className="h-4 w-4 rounded border-gray-300 dark:border-[#3E4042] text-blue-500 focus:ring-blue-500 dark:bg-[#3A3B3C]"
                  />
                  <label htmlFor="rememberMe" className="ml-2 block text-sm text-gray-700 dark:text-[#B0B3B8]">
                    Remember me
                  </label>
                </div>
                <a
                  href="/forgot-password"
                  onClick={handleForgotPassword}
                  className="text-sm font-medium text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors hover:underline cursor-pointer"
                >
                  Forgot password?
                </a>
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
                    Signing in...
                  </>
                ) : (
                  <>
                    Sign In
                    <ArrowRightIcon className="w-4 h-4 ml-2" />
                  </>
                )}
              </button>

              {/* Sign Up Link */}
              <p className="text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
                Don't have an account?{' '}
                <Link to="/register" className="font-medium text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors hover:underline">
                  Sign up
                </Link>
              </p>

              {/* Back to Home */}
              <p className="text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
                <Link to="/" className="text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors hover:underline">
                  ← Back to Home
                </Link>
              </p>
            </form>
          </motion.div>
        </div>
      </div>
    </div>
  )
}

export default Login