provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

provider "azurerm" {
  features {}
  resource_provider_registrations = "none"
  alias                           = "postgres_network"
  subscription_id                 = var.aks_subscription_id
}

terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 5.0"
    }

    azuread = {
      source  = "hashicorp/azuread"
      version = "3.9.0"
    }
  }
}
