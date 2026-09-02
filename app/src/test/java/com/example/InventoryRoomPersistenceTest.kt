package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.InventoryDatabase
import com.example.data.db.InventoryItemDao
import com.example.data.db.ProductDao
import com.example.data.db.StockMovementDao
import com.example.data.model.InventoryItem
import com.example.data.model.Product
import com.example.data.repository.InventoryItemRepository
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InventoryRoomPersistenceTest {

    private lateinit var database: InventoryDatabase
    private lateinit var productDao: ProductDao
    private lateinit var movementDao: StockMovementDao
    private lateinit var itemDao: InventoryItemDao
    private lateinit var repository: InventoryRepository
    private lateinit var itemRepository: InventoryItemRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = database.productDao()
        movementDao = database.stockMovementDao()
        itemDao = database.inventoryItemDao()
        repository = InventoryRepository(productDao, movementDao, itemDao)
        itemRepository = InventoryItemRepository(itemDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPersistInventoryItemWithNameQuantityAndBarcode() = runBlocking {
        val insertedId = repository.insertItem(
            name = "Wireless Barcode Scanner",
            quantity = 25,
            barcode = "978020137962"
        )
        assertTrue(insertedId > 0)

        // Verify retrieval by ID
        val retrieved = repository.getItemById(insertedId)
        assertNotNull(retrieved)
        assertEquals("Wireless Barcode Scanner", retrieved?.name)
        assertEquals(25, retrieved?.quantity)
        assertEquals(25, retrieved?.currentStock)
        assertEquals("978020137962", retrieved?.barcode)

        // Verify retrieval by barcode
        val byBarcode = repository.getItemByBarcode("978020137962")
        assertNotNull(byBarcode)
        assertEquals(insertedId, byBarcode?.id)
        assertEquals("Wireless Barcode Scanner", byBarcode?.name)
    }

    @Test
    fun testUpdateInventoryItemQuantityPersistence() = runBlocking {
        val insertedId = repository.insertItem(
            name = "Thermal Receipt Paper Rolls",
            quantity = 50,
            barcode = "793573192014"
        )

        // Update quantity
        repository.updateInventoryItemQuantity(insertedId, 35)

        val updated = repository.getItemById(insertedId)
        assertNotNull(updated)
        assertEquals(35, updated?.quantity)
        assertEquals(35, updated?.currentStock)
    }

    @Test
    fun testSaveInventoryItemUpdatesWhenBarcodeExists() = runBlocking {
        val initialId = repository.saveInventoryItem(
            name = "Bluetooth Label Printer",
            quantity = 10,
            barcode = "880912345678"
        )

        // Save again with same barcode, updated quantity and name
        val secondId = repository.saveInventoryItem(
            name = "Bluetooth Label Printer Pro",
            quantity = 18,
            barcode = "880912345678"
        )

        assertEquals(initialId, secondId)

        val item = repository.getItemById(initialId)
        assertNotNull(item)
        assertEquals("Bluetooth Label Printer Pro", item?.name)
        assertEquals(18, item?.quantity)
    }

    @Test
    fun testInventoryItemRepositoryDirectPersistence() = runBlocking {
        val id = itemRepository.insert(
            name = "Steel Storage Bin",
            quantity = 42,
            barcode = "612345678901"
        )
        assertTrue(id > 0)

        val retrieved = itemRepository.getById(id)
        assertNotNull(retrieved)
        assertEquals("Steel Storage Bin", retrieved?.name)
        assertEquals(42, retrieved?.quantity)
        assertEquals("612345678901", retrieved?.barcode)

        // Update quantity
        itemRepository.updateQuantity(id, 60)
        val updated = itemRepository.getById(id)
        assertEquals(60, updated?.quantity)

        // Flow observation
        val allItems = itemRepository.allItems.first()
        assertEquals(1, allItems.size)
        assertEquals("Steel Storage Bin", allItems[0].name)

        // Delete item
        itemRepository.deleteById(id)
        val afterDelete = itemRepository.getById(id)
        assertNull(afterDelete)
    }

    @Test
    fun testInventoryItemsFlowEmitsPersistedData() = runBlocking {
        repository.insertItem("Item A", 5, "111111")
        repository.insertItem("Item B", 15, "222222")

        val items = repository.inventoryItems.first()
        assertEquals(2, items.size)
        assertTrue(items.any { it.name == "Item A" && it.quantity == 5 && it.barcode == "111111" })
        assertTrue(items.any { it.name == "Item B" && it.quantity == 15 && it.barcode == "222222" })
    }
}
