package controllers

import javax.inject.{Singleton, Inject}
import org.slf4j.{LoggerFactory, Logger}
import play.api.mvc._

@Singleton
class Application extends Controller {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Application])

  def index = Action {
    logger.info("Serving index page...")
    Ok(views.html.index())
  }
}
