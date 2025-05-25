package com.endsoul.ecommerce.domain

import java.util.UUID

object subcategory {
  final case class Subcategory(categoryId: UUID, name: String, slug: String)
}



