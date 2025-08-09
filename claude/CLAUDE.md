# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a multi-module Spring Boot application with three main modules:
- **Root module (ai-super-app)**: Main application with chat, auth, notifications, topics, users, and MCP server features
- **claude module**: Claude AI API integration service 
- **openai module**: OpenAI API integration service

The architecture follows hexagonal/clean architecture patterns with:
- `adapter/in`: Controllers and DTOs for incoming requests
- `adapter/out`: External service adapters and DTOs
- `application`: Business logic, use cases, services, and domain entities
- `infrastructure`: Configuration, repositories, external clients

## Development Commands

### Building and Testing
- `../gradlew build` - Build all modules
- `../gradlew test` - Run tests
- `../gradlew clean` - Clean build artifacts
- `../gradlew check` - Run all checks including tests

### Running Applications
- Main app runs on port 8080 (default Spring Boot port)
- Claude module runs on port 8082
- OpenAI module runs on separate port

### Module-specific Commands
From the claude directory:
- `../gradlew :claude:build` - Build only claude module
- `../gradlew :claude:test` - Test only claude module
- `../gradlew :openai:build` - Build only openai module

## Key Technologies

- **Spring Boot 3.4.0** with WebFlux (reactive)
- **Java 17** language level
- **R2DBC** for reactive database access with MySQL
- **Redis** for caching
- **Spring AI** integrations for Azure OpenAI, Vertex AI Gemini, and Anthropic
- **Model Context Protocol (MCP)** server implementation
- **JWT** authentication with Microsoft Teams integration
- **Lombok** for code generation

## Configuration

Applications use YAML configuration with profile-based imports:
- Main app imports: teams.yml, azure.yml, mysql.yml, redis.yml, claude-sonnet.yml, google-gcp-auth.yml, logging.yml
- Claude module imports: claude-sonnet.yml, google-gcp-auth.yml

CORS is configured to allow localhost:3000, localhost:3002, and vscode-file://vscode-app origins.

## Important Patterns

- All entities follow audit patterns with BaseAuditEntity/BaseDateEntity
- ULID used for entity IDs instead of UUIDs
- Reactive programming with WebFlux throughout
- Cache repositories extend BaseCacheRepository
- Request/response DTOs are separated from domain entities
- Global exception handling with custom error codes