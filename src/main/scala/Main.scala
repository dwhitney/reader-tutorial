package tutorial

import Context._

object Main extends App{

  yahooQuoteRepository.createTable

  server.start(port,host)
  yahooFinanceService.pollAndUpdate(yahooQueueURL)

}
