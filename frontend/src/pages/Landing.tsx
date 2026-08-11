// src/pages/Landing.tsx
import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion, useAnimation } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import { useTheme } from '../context/ThemeContext'
import Lottie from 'react-lottie-player'
import chatAnimation from '../assets/lottie/chat-animation.json'
import { 
  ChatBubbleLeftRightIcon, 
  ShieldCheckIcon, 
  UsersIcon,
  CameraIcon,
  MusicalNoteIcon,
  ArrowRightIcon,
  SunIcon,
  MoonIcon,
  DevicePhoneMobileIcon,
  GlobeAltIcon,
  ClockIcon,
  SparklesIcon,
  UserPlusIcon,
  CloudArrowUpIcon,
  LockClosedIcon,
  WifiIcon,
  Bars3Icon,
  XMarkIcon
} from '@heroicons/react/24/outline'

// Feature data
const features = [
  {
    icon: ChatBubbleLeftRightIcon,
    title: 'Real-time Messaging',
    description: 'Send and receive messages instantly with real-time updates and typing indicators.',
    color: 'text-blue-500',
    bgColor: 'bg-blue-50 dark:bg-blue-900/20'
  },
  {
    icon: ShieldCheckIcon,
    title: 'End-to-End Encryption',
    description: 'Your conversations are private and secure with industry-standard encryption.',
    color: 'text-purple-500',
    bgColor: 'bg-purple-50 dark:bg-purple-900/20'
  },
  {
    icon: UsersIcon,
    title: 'Group Chats',
    description: 'Create groups and stay connected with friends, family, and colleagues.',
    color: 'text-pink-500',
    bgColor: 'bg-pink-50 dark:bg-pink-900/20'
  },
  {
    icon: CameraIcon,
    title: 'Media Sharing',
    description: 'Share photos, videos, and documents with your contacts effortlessly.',
    color: 'text-orange-500',
    bgColor: 'bg-orange-50 dark:bg-orange-900/20'
  },
  {
    icon: MusicalNoteIcon,
    title: 'Voice & Video Calls',
    description: 'High-quality voice and video calls with crystal clear audio.',
    color: 'text-emerald-500',
    bgColor: 'bg-emerald-50 dark:bg-emerald-900/20'
  },
  {
    icon: DevicePhoneMobileIcon,
    title: 'Cross-Platform',
    description: 'Access your chats from anywhere, on any device, seamlessly.',
    color: 'text-indigo-500',
    bgColor: 'bg-indigo-50 dark:bg-indigo-900/20'
  }
]

const steps = [
  {
    number: '01',
    title: 'Create Account',
    description: 'Sign up with your email and create your unique username.',
    icon: UserPlusIcon,
    color: 'bg-blue-500'
  },
  {
    number: '02',
    title: 'Find Friends',
    description: 'Search for friends by username or email and send friend requests.',
    icon: UsersIcon,
    color: 'bg-purple-500'
  },
  {
    number: '03',
    title: 'Start Chatting',
    description: 'Start sending messages, sharing media, and making calls instantly.',
    icon: SparklesIcon,
    color: 'bg-pink-500'
  }
]

const stats = [
  { label: 'Active Users', value: '1M+', icon: UsersIcon },
  { label: 'Messages Sent', value: '10B+', icon: ChatBubbleLeftRightIcon },
  { label: 'Countries', value: '190+', icon: GlobeAltIcon },
  { label: 'Secure Chats', value: '100%', icon: LockClosedIcon },
]

const AnimatedSection: React.FC<{ children: React.ReactNode; delay?: number }> = ({ 
  children, 
  delay = 0 
}) => {
  const controls = useAnimation()
  const [ref, inView] = useInView({
    triggerOnce: true,
    threshold: 0.1
  })

  useEffect(() => {
    if (inView) {
      controls.start('visible')
    }
  }, [controls, inView])

  return (
    <motion.div
      ref={ref}
      animate={controls}
      initial="hidden"
      variants={{
        visible: { opacity: 1, y: 0 },
        hidden: { opacity: 0, y: 50 }
      }}
      transition={{ duration: 0.6, delay, ease: "easeOut" }}
    >
      {children}
    </motion.div>
  )
}

const Landing = () => {
  const { theme, toggleTheme } = useTheme()
  const [isHovered, setIsHovered] = useState(false)
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <div className="min-h-screen overflow-x-hidden">
      {/* Navigation */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 dark:bg-[#242526]/95 backdrop-blur-sm border-b border-gray-100 dark:border-[#3E4042]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center space-x-2 flex-shrink-0">
              <div className="w-8 h-8 sm:w-9 sm:h-9 bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl flex items-center justify-center">
                <span className="text-white font-bold text-base sm:text-lg">W</span>
              </div>
              <span className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white">
                ChatApp
              </span>
            </div>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center space-x-4">
              <button
                onClick={toggleTheme}
                className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-[#3A3B3C] transition-colors"
                aria-label="Toggle theme"
              >
                {theme === 'dark' ? (
                  <SunIcon className="w-5 h-5 text-yellow-400" />
                ) : (
                  <MoonIcon className="w-5 h-5 text-gray-700" />
                )}
              </button>
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-medium text-gray-600 dark:text-[#B0B3B8] hover:text-gray-900 dark:hover:text-white transition-colors"
              >
                Log In
              </Link>
              <Link
                to="/register"
                className="px-5 py-2 text-sm font-medium text-white bg-blue-500 rounded-xl hover:bg-blue-600 transition-colors shadow-sm hover:shadow-md"
              >
                Get Started
              </Link>
            </div>

            {/* Mobile Menu Button */}
            <div className="flex md:hidden items-center space-x-3">
              <button
                onClick={toggleTheme}
                className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-[#3A3B3C] transition-colors"
                aria-label="Toggle theme"
              >
                {theme === 'dark' ? (
                  <SunIcon className="w-5 h-5 text-yellow-400" />
                ) : (
                  <MoonIcon className="w-5 h-5 text-gray-700" />
                )}
              </button>
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-[#3A3B3C] transition-colors"
                aria-label="Toggle menu"
              >
                {mobileMenuOpen ? (
                  <XMarkIcon className="w-6 h-6 text-gray-700 dark:text-white" />
                ) : (
                  <Bars3Icon className="w-6 h-6 text-gray-700 dark:text-white" />
                )}
              </button>
            </div>
          </div>

          {/* Mobile Menu */}
          {mobileMenuOpen && (
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="md:hidden py-4 border-t border-gray-100 dark:border-[#3E4042]"
            >
              <div className="flex flex-col space-y-3">
                <Link
                  to="/login"
                  className="px-4 py-2 text-center text-gray-600 dark:text-[#B0B3B8] hover:bg-gray-50 dark:hover:bg-[#3A3B3C] rounded-lg transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Log In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-center text-white bg-blue-500 rounded-lg hover:bg-blue-600 transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Get Started
                </Link>
              </div>
            </motion.div>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-20 sm:pt-24 md:pt-28 pb-12 sm:pb-16 px-4 sm:px-6 lg:px-8 bg-gradient-to-b from-blue-50/50 to-white dark:from-[#18191A] dark:to-[#1F2022]">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 sm:gap-12 items-center">
            <AnimatedSection>
              <div className="text-center lg:text-left">
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ duration: 0.5, type: "spring", stiffness: 200 }}
                  className="inline-flex items-center px-3 sm:px-4 py-1.5 sm:py-2 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-400 text-xs sm:text-sm font-medium mb-4 sm:mb-6 border border-blue-100 dark:border-blue-800"
                >
                  <SparklesIcon className="w-3 h-3 sm:w-4 sm:h-4 mr-1.5 sm:mr-2" />
                  <span>Welcome to the future of messaging</span>
                </motion.div>
                <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 dark:text-white mb-4 sm:mb-6 leading-tight">
                  Connect with the{' '}
                  <span className="text-blue-500">
                    World
                  </span>{' '}
                  instantly
                </h1>
                <p className="text-base sm:text-lg text-gray-600 dark:text-[#B0B3B8] mb-6 sm:mb-8 leading-relaxed max-w-lg mx-auto lg:mx-0">
                  Experience the next generation of messaging. Real-time chat, 
                  secure calls, and seamless file sharing — all in one place.
                </p>
                <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center lg:justify-start">
                  <Link
                    to="/register"
                    className="group px-6 sm:px-8 py-3 sm:py-3.5 bg-blue-500 text-white rounded-xl font-medium hover:bg-blue-600 transition-all inline-flex items-center justify-center shadow-sm hover:shadow-md text-sm sm:text-base"
                    onMouseEnter={() => setIsHovered(true)}
                    onMouseLeave={() => setIsHovered(false)}
                  >
                    Get Started Free
                    <ArrowRightIcon className={`w-4 h-4 ml-2 transition-transform duration-200 ${isHovered ? 'translate-x-1' : ''}`} />
                  </Link>
                  <Link
                    to="/login"
                    className="px-6 sm:px-8 py-3 sm:py-3.5 border border-gray-300 dark:border-[#3E4042] rounded-xl font-medium text-gray-700 dark:text-[#B0B3B8] hover:bg-gray-50 dark:hover:bg-[#3A3B3C] transition-all inline-flex items-center justify-center text-sm sm:text-base"
                  >
                    <GlobeAltIcon className="w-4 h-4 mr-2" />
                    Log In
                  </Link>
                </div>
                <div className="flex flex-wrap items-center justify-center lg:justify-start gap-4 sm:gap-6 mt-6 sm:mt-8">
                  <div className="flex items-center space-x-2">
                    <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-green-50 dark:bg-green-900/20 flex items-center justify-center">
                      <ClockIcon className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-green-600 dark:text-green-400" />
                    </div>
                    <span className="text-xs sm:text-sm text-gray-600 dark:text-[#B0B3B8]">24/7 available</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
                      <UsersIcon className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-blue-600 dark:text-blue-400" />
                    </div>
                    <span className="text-xs sm:text-sm text-gray-600 dark:text-[#B0B3B8]">1M+ users</span>
                  </div>
                </div>
              </div>
            </AnimatedSection>

            <AnimatedSection delay={0.2}>
              <div className="relative mt-8 lg:mt-0">
                {/* Lottie Animation */}
                <div className="relative bg-white dark:bg-[#242526] rounded-2xl p-4 sm:p-6 shadow-xl border border-gray-100 dark:border-[#3E4042]">
                  {chatAnimation ? (
                    <Lottie
                      loop
                      play
                      animationData={chatAnimation}
                      style={{ 
                        width: '100%', 
                        height: 'auto', 
                        maxHeight: '300px sm:max-h-[400px]',
                        display: 'block',
                        margin: '0 auto'
                      }}
                    />
                  ) : (
                    <div className="text-center py-12 sm:py-20 text-gray-500">
                      <p>Animation not available</p>
                    </div>
                  )}
                </div>

                {/* Floating Badge - Hidden on very small screens */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.8 }}
                  className="hidden xs:block absolute -bottom-2 -right-2 sm:-bottom-4 sm:-right-4 bg-white dark:bg-[#242526] rounded-xl shadow-lg p-2 sm:p-3 border border-gray-200 dark:border-[#3E4042]"
                >
                  <div className="flex items-center space-x-1.5 sm:space-x-2">
                    <div className="w-1.5 h-1.5 sm:w-2 sm:h-2 bg-green-500 rounded-full animate-pulse"></div>
                    <span className="text-xs sm:text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">10k+ online</span>
                  </div>
                </motion.div>
              </div>
            </AnimatedSection>
          </div>

          {/* Stats Section */}
          <AnimatedSection delay={0.4}>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 mt-10 sm:mt-16 pt-6 sm:pt-8 border-t border-gray-200 dark:border-[#3E4042]">
              {stats.map((stat, index) => (
                <motion.div
                  key={index}
                  whileHover={{ y: -2 }}
                  className="text-center p-3 sm:p-4 rounded-xl bg-white dark:bg-[#242526] border border-gray-100 dark:border-[#3E4042]"
                >
                  <div className="flex items-center justify-center mb-1 sm:mb-2">
                    <stat.icon className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500" />
                  </div>
                  <div className="text-lg sm:text-2xl font-bold text-gray-900 dark:text-white">{stat.value}</div>
                  <div className="text-xs sm:text-sm text-gray-600 dark:text-[#B0B3B8]">{stat.label}</div>
                </motion.div>
              ))}
            </div>
          </AnimatedSection>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-12 sm:py-16 md:py-20 px-4 sm:px-6 lg:px-8 bg-white dark:bg-[#18191A]">
        <div className="max-w-7xl mx-auto">
          <AnimatedSection>
            <div className="text-center mb-10 sm:mb-12 md:mb-16">
              <motion.div
                initial={{ scale: 0 }}
                whileInView={{ scale: 1 }}
                transition={{ duration: 0.5 }}
                className="inline-flex items-center px-3 sm:px-4 py-1.5 sm:py-2 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-400 text-xs sm:text-sm font-medium mb-3 sm:mb-4"
              >
                <SparklesIcon className="w-3 h-3 sm:w-4 sm:h-4 mr-1.5 sm:mr-2" />
                Features
              </motion.div>
              <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">
                Everything you need to{' '}
                <span className="text-blue-500">
                  stay connected
                </span>
              </h2>
              <p className="text-base sm:text-lg text-gray-600 dark:text-[#B0B3B8] max-w-2xl mx-auto px-4">
                Powerful features designed to make your conversations better, faster, and more secure.
              </p>
            </div>
          </AnimatedSection>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
            {features.map((feature, index) => (
              <AnimatedSection key={index} delay={index * 0.1}>
                <motion.div
                  whileHover={{ y: -4 }}
                  className={`p-5 sm:p-6 ${feature.bgColor} rounded-xl border border-gray-100 dark:border-[#3E4042] transition-all hover:shadow-md`}
                >
                  <div className={`w-10 h-10 sm:w-12 sm:h-12 ${feature.color} bg-opacity-10 rounded-xl flex items-center justify-center mb-3 sm:mb-4`}>
                    <feature.icon className="w-5 h-5 sm:w-6 sm:h-6" />
                  </div>
                  <h3 className="text-base sm:text-lg font-semibold text-gray-900 dark:text-white mb-1.5 sm:mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-sm sm:text-base text-gray-600 dark:text-[#B0B3B8] leading-relaxed">
                    {feature.description}
                  </p>
                </motion.div>
              </AnimatedSection>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-12 sm:py-16 md:py-20 px-4 sm:px-6 lg:px-8 bg-gray-50 dark:bg-[#1F2022]">
        <div className="max-w-7xl mx-auto">
          <AnimatedSection>
            <div className="text-center mb-10 sm:mb-12 md:mb-16">
              <motion.div
                initial={{ scale: 0 }}
                whileInView={{ scale: 1 }}
                transition={{ duration: 0.5 }}
                className="inline-flex items-center px-3 sm:px-4 py-1.5 sm:py-2 rounded-full bg-purple-50 dark:bg-purple-900/20 text-purple-700 dark:text-purple-400 text-xs sm:text-sm font-medium mb-3 sm:mb-4"
              >
                <WifiIcon className="w-3 h-3 sm:w-4 sm:h-4 mr-1.5 sm:mr-2" />
                How it works
              </motion.div>
              <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">
                Get started in{' '}
                <span className="text-blue-500">
                  three simple steps
                </span>
              </h2>
              <p className="text-base sm:text-lg text-gray-600 dark:text-[#B0B3B8] max-w-2xl mx-auto px-4">
                Join millions of users who trust ChatApp for their daily conversations
              </p>
            </div>
          </AnimatedSection>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 sm:gap-8">
            {steps.map((step, index) => (
              <AnimatedSection key={index} delay={index * 0.2}>
                <motion.div
                  whileHover={{ y: -4 }}
                  className="relative text-center p-5 sm:p-6 bg-white dark:bg-[#242526] rounded-xl border border-gray-100 dark:border-[#3E4042] shadow-sm"
                >
                  <div className="relative">
                    <div className={`w-14 h-14 sm:w-16 sm:h-16 ${step.color} rounded-xl flex items-center justify-center mx-auto mb-3 sm:mb-4`}>
                      <step.icon className="w-6 h-6 sm:w-7 sm:h-7 text-white" />
                    </div>
                    {index < steps.length - 1 && (
                      <div className="hidden md:block absolute top-7 sm:top-8 left-[65%] w-[35%] h-0.5 bg-gray-300 dark:bg-[#3E4042]">
                        <div className="w-2 h-2 bg-blue-500 rounded-full -mt-0.5 mx-auto"></div>
                      </div>
                    )}
                    <div className="absolute -top-2 -right-2 w-6 h-6 sm:w-7 sm:h-7 bg-blue-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
                      {step.number}
                    </div>
                  </div>
                  <h3 className="text-base sm:text-lg font-semibold text-gray-900 dark:text-white mb-1.5 sm:mb-2">
                    {step.title}
                  </h3>
                  <p className="text-sm sm:text-base text-gray-600 dark:text-[#B0B3B8] leading-relaxed">
                    {step.description}
                  </p>
                </motion.div>
              </AnimatedSection>
            ))}
          </div>

          <AnimatedSection delay={0.6}>
            <div className="text-center mt-8 sm:mt-10 md:mt-12">
              <Link
                to="/register"
                className="inline-flex items-center px-6 sm:px-8 py-3 sm:py-3.5 bg-blue-500 text-white rounded-xl font-medium hover:bg-blue-600 transition-all shadow-sm hover:shadow-md text-sm sm:text-base"
              >
                Start Your Journey Now
                <CloudArrowUpIcon className="w-4 h-4 sm:w-5 sm:h-5 ml-2" />
              </Link>
            </div>
          </AnimatedSection>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-white dark:bg-[#242526] border-t border-gray-200 dark:border-[#3E4042] py-8 sm:py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 sm:gap-8">
            <div className="col-span-2 sm:col-span-1">
              <div className="flex items-center space-x-2 mb-3 sm:mb-4">
                <div className="w-7 h-7 sm:w-8 sm:h-8 bg-blue-500 rounded-xl flex items-center justify-center">
                  <span className="text-white font-bold text-sm sm:text-base">W</span>
                </div>
                <span className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white">
                  ChatApp
                </span>
              </div>
              <p className="text-sm text-gray-600 dark:text-[#B0B3B8] leading-relaxed">
                The future of messaging. Connect with anyone, anywhere.
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-3 sm:mb-4 text-sm sm:text-base">Product</h4>
              <ul className="space-y-2 text-sm text-gray-600 dark:text-[#B0B3B8]">
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Features</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Security</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Pricing</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Updates</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-3 sm:mb-4 text-sm sm:text-base">Company</h4>
              <ul className="space-y-2 text-sm text-gray-600 dark:text-[#B0B3B8]">
                <li className="hover:text-blue-500 transition-colors cursor-pointer">About</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Blog</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Careers</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Contact</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-3 sm:mb-4 text-sm sm:text-base">Legal</h4>
              <ul className="space-y-2 text-sm text-gray-600 dark:text-[#B0B3B8]">
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Privacy</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Terms</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Security</li>
                <li className="hover:text-blue-500 transition-colors cursor-pointer">Cookies</li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-200 dark:border-[#3E4042] mt-6 sm:mt-8 pt-6 sm:pt-8 text-center text-xs sm:text-sm text-gray-600 dark:text-[#B0B3B8]">
            © 2026 ChatApp. All rights reserved. Made with ❤️
          </div>
        </div>
      </footer>
    </div>
  )
}

export default Landing