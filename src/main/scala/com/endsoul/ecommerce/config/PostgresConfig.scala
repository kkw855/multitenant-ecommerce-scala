package com.endsoul.ecommerce.config

import pureconfig.ConfigReader

final case class PostgresConfig (nThreads: Int, url: String, user: String, pass: String) derives ConfigReader
