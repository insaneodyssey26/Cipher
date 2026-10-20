package com.masum.cipher.core.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.LucideIcons
import compose.icons.lucideicons.Activity
import compose.icons.lucideicons.Apple
import compose.icons.lucideicons.Baby
import compose.icons.lucideicons.Banana
import compose.icons.lucideicons.Banknote
import compose.icons.lucideicons.Beer
import compose.icons.lucideicons.Bike
import compose.icons.lucideicons.BookOpen
import compose.icons.lucideicons.Briefcase
import compose.icons.lucideicons.BriefcaseMedical
import compose.icons.lucideicons.Brush
import compose.icons.lucideicons.Building
import compose.icons.lucideicons.Bus
import compose.icons.lucideicons.CableCar
import compose.icons.lucideicons.Cake
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Camera
import compose.icons.lucideicons.Car
import compose.icons.lucideicons.CarFront
import compose.icons.lucideicons.CarTaxiFront
import compose.icons.lucideicons.Caravan
import compose.icons.lucideicons.Carrot
import compose.icons.lucideicons.Cat
import compose.icons.lucideicons.Clapperboard
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Coffee
import compose.icons.lucideicons.Coins
import compose.icons.lucideicons.Compass
import compose.icons.lucideicons.CreditCard
import compose.icons.lucideicons.CupSoda
import compose.icons.lucideicons.Dog
import compose.icons.lucideicons.Drill
import compose.icons.lucideicons.Droplets
import compose.icons.lucideicons.Dumbbell
import compose.icons.lucideicons.Fish
import compose.icons.lucideicons.Flame
import compose.icons.lucideicons.Fuel
import compose.icons.lucideicons.Gamepad
import compose.icons.lucideicons.Gamepad2
import compose.icons.lucideicons.Gem
import compose.icons.lucideicons.Gift
import compose.icons.lucideicons.Glasses
import compose.icons.lucideicons.GraduationCap
import compose.icons.lucideicons.Hammer
import compose.icons.lucideicons.HandCoins
import compose.icons.lucideicons.Headphones
import compose.icons.lucideicons.Heart
import compose.icons.lucideicons.HeartPulse
import compose.icons.lucideicons.Hotel
import compose.icons.lucideicons.House
import compose.icons.lucideicons.Key
import compose.icons.lucideicons.Landmark
import compose.icons.lucideicons.Laptop
import compose.icons.lucideicons.Lightbulb
import compose.icons.lucideicons.Lock
import compose.icons.lucideicons.Luggage
import compose.icons.lucideicons.MapPin
import compose.icons.lucideicons.Mic
import compose.icons.lucideicons.Music
import compose.icons.lucideicons.Package
import compose.icons.lucideicons.PaintRoller
import compose.icons.lucideicons.Palette
import compose.icons.lucideicons.PawPrint
import compose.icons.lucideicons.Percent
import compose.icons.lucideicons.Phone
import compose.icons.lucideicons.PiggyBank
import compose.icons.lucideicons.Pill
import compose.icons.lucideicons.Pizza
import compose.icons.lucideicons.Plane
import compose.icons.lucideicons.Popcorn
import compose.icons.lucideicons.Radio
import compose.icons.lucideicons.ReceiptText
import compose.icons.lucideicons.Recycle
import compose.icons.lucideicons.Sailboat
import compose.icons.lucideicons.Sandwich
import compose.icons.lucideicons.School
import compose.icons.lucideicons.Scissors
import compose.icons.lucideicons.Shapes
import compose.icons.lucideicons.Shield
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Ship
import compose.icons.lucideicons.Shirt
import compose.icons.lucideicons.ShoppingBag
import compose.icons.lucideicons.ShoppingCart
import compose.icons.lucideicons.Smartphone
import compose.icons.lucideicons.Smile
import compose.icons.lucideicons.Soup
import compose.icons.lucideicons.Sparkle
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Stethoscope
import compose.icons.lucideicons.Store
import compose.icons.lucideicons.Syringe
import compose.icons.lucideicons.Tablet
import compose.icons.lucideicons.Tag
import compose.icons.lucideicons.Thermometer
import compose.icons.lucideicons.Ticket
import compose.icons.lucideicons.TrainFront
import compose.icons.lucideicons.Trash
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.Trophy
import compose.icons.lucideicons.Tv
import compose.icons.lucideicons.Users
import compose.icons.lucideicons.Utensils
import compose.icons.lucideicons.UtensilsCrossed
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.Warehouse
import compose.icons.lucideicons.Watch
import compose.icons.lucideicons.Wifi
import compose.icons.lucideicons.Wine
import compose.icons.lucideicons.Wrench
import compose.icons.lucideicons.Zap

object CategoryIconRegistry {
    val ICONS: List<Pair<String, ImageVector>> = listOf(
        "Activity" to LucideIcons.Activity,
        "Apple" to LucideIcons.Apple,
        "Baby" to LucideIcons.Baby,
        "Banana" to LucideIcons.Banana,
        "Banknote" to LucideIcons.Banknote,
        "Beer" to LucideIcons.Beer,
        "Bike" to LucideIcons.Bike,
        "BookOpen" to LucideIcons.BookOpen,
        "Briefcase" to LucideIcons.Briefcase,
        "BriefcaseMedical" to LucideIcons.BriefcaseMedical,
        "Brush" to LucideIcons.Brush,
        "Building" to LucideIcons.Building,
        "Bus" to LucideIcons.Bus,
        "CableCar" to LucideIcons.CableCar,
        "Cake" to LucideIcons.Cake,
        "Calendar" to LucideIcons.Calendar,
        "Camera" to LucideIcons.Camera,
        "Car" to LucideIcons.Car,
        "CarFront" to LucideIcons.CarFront,
        "CarTaxiFront" to LucideIcons.CarTaxiFront,
        "Caravan" to LucideIcons.Caravan,
        "Carrot" to LucideIcons.Carrot,
        "Cat" to LucideIcons.Cat,
        "Clapperboard" to LucideIcons.Clapperboard,
        "Clock" to LucideIcons.Clock,
        "Coffee" to LucideIcons.Coffee,
        "Coins" to LucideIcons.Coins,
        "Compass" to LucideIcons.Compass,
        "CreditCard" to LucideIcons.CreditCard,
        "CupSoda" to LucideIcons.CupSoda,
        "Dog" to LucideIcons.Dog,
        "Drill" to LucideIcons.Drill,
        "Droplets" to LucideIcons.Droplets,
        "Dumbbell" to LucideIcons.Dumbbell,
        "Fish" to LucideIcons.Fish,
        "Flame" to LucideIcons.Flame,
        "Fuel" to LucideIcons.Fuel,
        "Gamepad" to LucideIcons.Gamepad,
        "Gamepad2" to LucideIcons.Gamepad2,
        "Gem" to LucideIcons.Gem,
        "Gift" to LucideIcons.Gift,
        "Glasses" to LucideIcons.Glasses,
        "GraduationCap" to LucideIcons.GraduationCap,
        "Hammer" to LucideIcons.Hammer,
        "HandCoins" to LucideIcons.HandCoins,
        "Headphones" to LucideIcons.Headphones,
        "Heart" to LucideIcons.Heart,
        "HeartPulse" to LucideIcons.HeartPulse,
        "Hotel" to LucideIcons.Hotel,
        "House" to LucideIcons.House,
        "Key" to LucideIcons.Key,
        "Landmark" to LucideIcons.Landmark,
        "Laptop" to LucideIcons.Laptop,
        "Lightbulb" to LucideIcons.Lightbulb,
        "Lock" to LucideIcons.Lock,
        "Luggage" to LucideIcons.Luggage,
        "MapPin" to LucideIcons.MapPin,
        "Mic" to LucideIcons.Mic,
        "Music" to LucideIcons.Music,
        "Package" to LucideIcons.Package,
        "PaintRoller" to LucideIcons.PaintRoller,
        "Palette" to LucideIcons.Palette,
        "PawPrint" to LucideIcons.PawPrint,
        "Percent" to LucideIcons.Percent,
        "Phone" to LucideIcons.Phone,
        "PiggyBank" to LucideIcons.PiggyBank,
        "Pill" to LucideIcons.Pill,
        "Pizza" to LucideIcons.Pizza,
        "Plane" to LucideIcons.Plane,
        "Popcorn" to LucideIcons.Popcorn,
        "Radio" to LucideIcons.Radio,
        "ReceiptText" to LucideIcons.ReceiptText,
        "Recycle" to LucideIcons.Recycle,
        "Sailboat" to LucideIcons.Sailboat,
        "Sandwich" to LucideIcons.Sandwich,
        "School" to LucideIcons.School,
        "Scissors" to LucideIcons.Scissors,
        "Shapes" to LucideIcons.Shapes,
        "Shield" to LucideIcons.Shield,
        "ShieldCheck" to LucideIcons.ShieldCheck,
        "Ship" to LucideIcons.Ship,
        "Shirt" to LucideIcons.Shirt,
        "ShoppingBag" to LucideIcons.ShoppingBag,
        "ShoppingCart" to LucideIcons.ShoppingCart,
        "Smartphone" to LucideIcons.Smartphone,
        "Smile" to LucideIcons.Smile,
        "Soup" to LucideIcons.Soup,
        "Sparkle" to LucideIcons.Sparkle,
        "Sparkles" to LucideIcons.Sparkles,
        "Stethoscope" to LucideIcons.Stethoscope,
        "Store" to LucideIcons.Store,
        "Syringe" to LucideIcons.Syringe,
        "Tablet" to LucideIcons.Tablet,
        "Tag" to LucideIcons.Tag,
        "Thermometer" to LucideIcons.Thermometer,
        "Ticket" to LucideIcons.Ticket,
        "TrainFront" to LucideIcons.TrainFront,
        "Trash" to LucideIcons.Trash,
        "TrendingUp" to LucideIcons.TrendingUp,
        "Trophy" to LucideIcons.Trophy,
        "Tv" to LucideIcons.Tv,
        "Users" to LucideIcons.Users,
        "Utensils" to LucideIcons.Utensils,
        "UtensilsCrossed" to LucideIcons.UtensilsCrossed,
        "Wallet" to LucideIcons.Wallet,
        "Warehouse" to LucideIcons.Warehouse,
        "Watch" to LucideIcons.Watch,
        "Wifi" to LucideIcons.Wifi,
        "Wine" to LucideIcons.Wine,
        "Wrench" to LucideIcons.Wrench,
        "Zap" to LucideIcons.Zap
    )

    private val ICON_MAP: Map<String, ImageVector> = ICONS.toMap()

    fun getIcon(iconName: String?): ImageVector {
        return ICON_MAP[iconName] ?: LucideIcons.Shapes
    }
}

object CategoryColorRegistry {
    val COLORS: List<Long> = listOf(
        0xFFFF5722L,
        0xFFFF7043L,
        0xFFFFA726L,
        0xFFFFCA28L,
        0xFFEF5350L,
        0xFFE53935L,
        0xFFEC407AL,
        0xFFD81B60L,
        0xFFAB47BCL,
        0xFF8E24AAL,
        0xFF7E57C2L,
        0xFF5C6BC0L,
        0xFF3F51B5L,
        0xFF283593L,
        0xFF42A5F5L,
        0xFF1E88E5L,
        0xFF00ACC1L,
        0xFF26C6DAL,
        0xFF26A69AL,
        0xFF00897BL,
        0xFF43A047L,
        0xFF66BB6AL,
        0xFF2E7D32L,
        0xFF7CB342L,
        0xFFC0CA33L,
        0xFFFDD835L,
        0xFF8D6E63L,
        0xFF6D4C41L,
        0xFF78909CL,
        0xFF546E7AL,
        0xFF455A64L,
        0xFF263238L
    )
}