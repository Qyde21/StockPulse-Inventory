package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.MovementType
import com.example.data.model.Product
import com.example.data.model.StockMovement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Product::class, StockMovement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "stockpulse_inventory.db"
                )
                    .addCallback(InventoryDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val INITIAL_PRODUCTS = listOf(
            Product(
                barcode = "8901030381023",
                sku = "BEV-001",
                name = "Organic Cold Brew Coffee 330ml",
                category = "Beverages",
                costPrice = 1.45,
                sellingPrice = 3.99,
                currentStock = 3, // LOW STOCK ALERT (Threshold 8)
                reorderThreshold = 8,
                idealStock = 40,
                unit = "bottle",
                locationRack = "Aisle 1 - Shelf A",
                supplier = "Artisan Roasters Inc",
                notes = "High demand summer item"
            ),
            Product(
                barcode = "7622210449283",
                sku = "SNK-002",
                name = "Dark Sea Salt Chocolate Bar 100g",
                category = "Groceries & Food",
                costPrice = 1.20,
                sellingPrice = 2.79,
                currentStock = 0, // CRITICAL OUT OF STOCK ALERT
                reorderThreshold = 10,
                idealStock = 50,
                unit = "pcs",
                locationRack = "Aisle 2 - Shelf C",
                supplier = "Swiss Confections Co",
                notes = "Sold out yesterday, supplier contacted"
            ),
            Product(
                barcode = "6941059632847",
                sku = "ELC-003",
                name = "Braided USB-C Fast Charging Cable 2m",
                category = "Electronics & Acc",
                costPrice = 2.80,
                sellingPrice = 9.99,
                currentStock = 4, // LOW STOCK ALERT
                reorderThreshold = 12,
                idealStock = 45,
                unit = "pack",
                locationRack = "Counter Display 1",
                supplier = "VoltTech Components",
                notes = "Top retail margin item"
            ),
            Product(
                barcode = "5000159482104",
                sku = "BEV-004",
                name = "Sparkling Lime Mineral Water 500ml",
                category = "Beverages",
                costPrice = 0.65,
                sellingPrice = 1.89,
                currentStock = 24,
                reorderThreshold = 12,
                idealStock = 60,
                unit = "bottle",
                locationRack = "Aisle 1 - Fridge 2",
                supplier = "PureSpring Distributors",
                notes = "Regular delivery every Tuesday"
            ),
            Product(
                barcode = "4005808781923",
                sku = "PC-005",
                name = "Hydrating Botanical Daily Hand Cream 75ml",
                category = "Personal Care & Health",
                costPrice = 3.50,
                sellingPrice = 8.50,
                currentStock = 2, // CRITICAL LOW STOCK
                reorderThreshold = 6,
                idealStock = 25,
                unit = "tube",
                locationRack = "Aisle 3 - Shelf B",
                supplier = "Naturals Beauty Ltd",
                notes = "Cruelty-free bestseller"
            ),
            Product(
                barcode = "8809568210342",
                sku = "STA-006",
                name = "Gel Ink Rollerball Pen Set (Pack of 5)",
                category = "Stationery & Office",
                costPrice = 2.10,
                sellingPrice = 5.49,
                currentStock = 18,
                reorderThreshold = 8,
                idealStock = 30,
                unit = "pack",
                locationRack = "Aisle 4 - Shelf A",
                supplier = "Apex Office Supplies",
                notes = "Black 0.5mm tip"
            ),
            Product(
                barcode = "9310055123984",
                sku = "HOM-007",
                name = "Eco Soy Wax Amber Candle (Cedar & Vanilla)",
                category = "Home & Hardware",
                costPrice = 4.20,
                sellingPrice = 12.99,
                currentStock = 11,
                reorderThreshold = 5,
                idealStock = 25,
                unit = "jar",
                locationRack = "Aisle 4 - Shelf D",
                supplier = "Homestead Aromas",
                notes = "40h burn time"
            ),
            Product(
                barcode = "7310865004721",
                sku = "SNK-008",
                name = "Raw Almond & Cranberry Trail Mix 200g",
                category = "Groceries & Food",
                costPrice = 1.95,
                sellingPrice = 4.25,
                currentStock = 1, // CRITICAL LOW STOCK
                reorderThreshold = 8,
                idealStock = 35,
                unit = "pack",
                locationRack = "Aisle 2 - Shelf A",
                supplier = "SunHarvest Organics",
                notes = "Gluten free"
            ),
            Product(
                barcode = "8710103859214",
                sku = "ELC-009",
                name = "Magnetic Phone Mount for Car Air Vent",
                category = "Electronics & Acc",
                costPrice = 3.90,
                sellingPrice = 14.50,
                currentStock = 15,
                reorderThreshold = 6,
                idealStock = 30,
                unit = "pcs",
                locationRack = "Counter Display 2",
                supplier = "VoltTech Components",
                notes = "Universal 360 rotation"
            ),
            Product(
                barcode = "5411188119042",
                sku = "BEV-010",
                name = "Almond Milk Unsweetened Barista Blend 1L",
                category = "Beverages",
                costPrice = 1.35,
                sellingPrice = 3.49,
                currentStock = 0, // OUT OF STOCK
                reorderThreshold = 10,
                idealStock = 40,
                unit = "carton",
                locationRack = "Aisle 1 - Shelf C",
                supplier = "PureSpring Distributors",
                notes = "Key café & retail stock"
            )
        )
    }

    private class InventoryDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.productDao(), database.stockMovementDao())
                }
            }
        }

        suspend fun populateInitialData(productDao: ProductDao, movementDao: StockMovementDao) {
            INITIAL_PRODUCTS.forEach { product ->
                val insertedId = productDao.insertProduct(product)
                // Add initial stock movement log
                movementDao.insertMovement(
                    StockMovement(
                        productId = insertedId,
                        productName = product.name,
                        barcode = product.barcode,
                        type = MovementType.STOCK_IN,
                        quantityDelta = product.currentStock,
                        previousStock = 0,
                        newStock = product.currentStock,
                        reason = "Initial Inventory Intake",
                        unitPrice = product.costPrice,
                        timestamp = System.currentTimeMillis() - (86400000L * (1..3).random())
                    )
                )
            }
        }
    }
}
