package slices

import common.Maps._
import model.Content

import scala.util.Try

object Story {
  implicit val ordering = Ordering.by[Story, Int](_.group)

  def unboosted(n: Int) = Story(n, isBoosted = false)

  private [slices] def segmentByGroup(stories: Seq[Story]): Map[Int, Seq[Story]] = {
    stories.foldLeft(Map.empty[Int, Seq[Story]]) { (acc, story) =>
      insertWith(acc, story.group, Seq(story)) { (a, b) =>
        b ++ a
      }
    }
  }

  def fromContent(content: Content): Story = {
    Story(
      /** Stories that are not assigned to a group are treated as standard (0) items */
      content.group.flatMap(group => Try(group.toInt).toOption).getOrElse(0),
      content.isBoosted
    )
  }
}

case class Story(
  group: Int,
  isBoosted: Boolean
)
