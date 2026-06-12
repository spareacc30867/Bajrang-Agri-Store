package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val inquiryDao: InquiryDao) {
    val allInquiries: Flow<List<Inquiry>> = inquiryDao.getAllInquiries()

    suspend fun insertInquiry(inquiry: Inquiry) {
        inquiryDao.insertInquiry(inquiry)
    }

    suspend fun deleteInquiryById(id: Int) {
        inquiryDao.deleteInquiryById(id)
    }
}
