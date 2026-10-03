variable "project_id" {
  type        = string
  
}

variable "image" {
  type = string
}


variable "instance_count" {
  type = number
  default = 1
}

variable "app_env" {
  type = map(string)
  sensitive = true 
}


variable "broker_count" {
  type    = number
  default = 3
}
