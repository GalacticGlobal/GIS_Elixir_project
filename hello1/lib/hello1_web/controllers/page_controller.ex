defmodule Hello1Web.PageController do
  use Hello1Web, :controller

  def index(conn, _params) do
    render(conn, "index.html",pagename: "Welcome to Galactic")
  end
end
