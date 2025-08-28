    private void updateRect() {
      path.reset();

      if (rectSize == null) {
        return;
      }

      PointF size = rectSize.getValue();
      float halfWidth = size.x / 2f;
      float halfHeight = size.y / 2f;
      float radius = rectCornerRadius == null ? 0f : rectCornerRadius.getValue();

      // Draw the rectangle top right to bottom left.
      PointF position = rectPosition == null ? Utils.emptyPoint() : rectPosition.getValue();

      path.moveTo(position.x + halfWidth, position.y - halfHeight + radius);

      path.lineTo(position.x + halfWidth, position.y + halfHeight - radius);

      if (radius > 0) {
        rect.set(position.x + halfWidth - 2 * radius,
            position.y + halfHeight - 2 * radius,
            position.x + halfWidth,
            position.y + halfHeight);
        path.arcTo(rect, 0, 90, false);
      }

      path.lineTo(position.x - halfWidth + radius, position.y + halfHeight);

      if (radius > 0) {
        rect.set(position.x - halfWidth,
            position.y + halfHeight - 2 * radius,
            position.x - halfWidth + 2 * radius,
            position.y + halfHeight);
        path.arcTo(rect, 90, 90, false);
      }

      path.lineTo(position.x - halfWidth, position.y - halfHeight + 2 * radius);

      if (radius > 0) {
        rect.set(position.x - halfWidth,
            position.y - halfHeight,
            position.x - halfWidth + 2 * radius,
            position.y - halfHeight + 2 * radius);
        path.arcTo(rect, 180, 90, false);
      }

      path.lineTo(position.x + halfWidth - 2 * radius, position.y - halfHeight);

      if (radius > 0) {
        rect.set(position.x + halfWidth - 2 * radius,
            position.y - halfHeight,
            position.x + halfWidth,
            position.y - halfHeight + 2 * radius);
        path.arcTo(rect, 270, 90, false);
      }
      path.close();

      onPathChanged();
    }
