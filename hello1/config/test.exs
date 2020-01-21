use Mix.Config

# We don't run a server during test. If one is required,
# you can enable the server option below.
config :hello1, Hello1Web.Endpoint,
  http: [port: 4002],
  server: false

# Print only warnings and errors during test
config :logger, level: :warn

# Configure your database
config :hello1, Hello1.Repo,
  username: "hello_web",
  password: "galactic123",
  database: "hello1_dev",
  hostname: "localhost",
  pool_size: 10
  pool: Ecto.Adapters.SQL.Sandbox
