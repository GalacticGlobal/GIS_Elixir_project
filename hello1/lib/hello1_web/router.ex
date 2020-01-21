defmodule Hello1Web.Router do
  use Hello1Web, :router

  pipeline :browser do
    plug :accepts, ["html"]
    plug :fetch_session
    plug :fetch_flash
    plug :protect_from_forgery
    plug :put_secure_browser_headers
  end

  pipeline :api do
    plug :accepts, ["json"]
  end

  scope "/", Hello1Web do
    pipe_through :browser

    resources "/register", UserController, only: [:create, :new]
    #resources "/show", UserController
    get "/show", SessionController, :show
    get "/registered", SessionController, :registered
    get "/login", SessionController, :new
    post "/login", SessionController, :create
    delete "/logout", SessionController, :delete
    get "/logout", SessionController, :delete

    get "/", PageController, :index
  end

  # Other scopes may use custom stacks.
  # scope "/api", Hello1Web do
  #   pipe_through :api
  # end
end
