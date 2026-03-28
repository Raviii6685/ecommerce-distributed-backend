#!/bin/bash

BASE_DIR="/Users/home_folder/project-replication/springboot-mongodb-example/src/main/java/zatribune/spring/ex_mongodb_docker"

# Find .java files only in controllers, entities, and dto folders
find "$BASE_DIR/controllers" "$BASE_DIR/entities" "$BASE_DIR/dto" -type f -name "*.java" | sort | while read f; do
    echo "=== $f ==="
    cat "$f"
done > /Users/home_folder/Desktop/controllers_entities_dtos_code.txt

echo "Successfully copied all controllers, entities, and DTOs to: /Users/home_folder/controllers_entities_dtos_code.txt"
